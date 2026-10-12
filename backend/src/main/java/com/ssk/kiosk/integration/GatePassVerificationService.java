package com.ssk.kiosk.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class GatePassVerificationService {
  private static final Logger logger = LoggerFactory.getLogger(GatePassVerificationService.class);
  private final IntegrationConfigurationService configurations;
  private final VerificationExecutionLogRepository logs;
  private final IntegrationWorkflowExecutor workflowExecutor;
  private final ObjectMapper mapper;

  public GatePassVerificationService(IntegrationConfigurationService configurations, VerificationExecutionLogRepository logs, IntegrationWorkflowExecutor workflowExecutor, ObjectMapper mapper) {
    this.configurations = configurations;
    this.logs = logs;
    this.workflowExecutor = workflowExecutor;
    this.mapper = mapper;
  }

  public GatePassVerificationResponse verify(String gatePassId, String kioskId) {
    String normalized = gatePassId == null ? "" : gatePassId.trim();
    if (normalized.isEmpty() || normalized.length() > 64 || !normalized.matches("[A-Za-z0-9._/-]+")) {
      throw new IllegalArgumentException("gatePassId must be 1 to 64 alphanumeric characters");
    }
    String correlationId = UUID.randomUUID().toString();
    CompletableFuture<NormalizedVerificationResult> tasreeh = asyncVerify("TASREEH", normalized, kioskId, correlationId);
    CompletableFuture<NormalizedVerificationResult> pangu = asyncVerify("PANGU", normalized, kioskId, correlationId);
    NormalizedVerificationResult tasreehResult = join(tasreeh, "TASREEH", normalized);
    NormalizedVerificationResult panguResult = join(pangu, "PANGU", normalized);
    Map<String, NormalizedVerificationResult> results = new LinkedHashMap<>();
    results.put("TASREEH", tasreehResult);
    results.put("PANGU", panguResult);
    String outcome;
    if (tasreehResult.verified() && panguResult.verified()) outcome = "APPROVED";
    else if (isUnavailable(tasreehResult) && isUnavailable(panguResult)) outcome = "VERIFICATION_UNAVAILABLE";
    else if (isUnavailable(tasreehResult) || isUnavailable(panguResult)) outcome = "PARTIAL_FAILURE";
    else outcome = "REJECTED";
    String message = "APPROVED".equals(outcome) ? "Gate Pass verified"
        : !tasreehResult.verified() ? tasreehResult.message() : panguResult.message();
    return new GatePassVerificationResponse(normalized, outcome, message, results);
  }

  private CompletableFuture<NormalizedVerificationResult> asyncVerify(String key, String gatePassId, String kioskId, String correlationId) {
    return CompletableFuture.supplyAsync(() -> execute(key, gatePassId, kioskId, correlationId));
  }

  private NormalizedVerificationResult execute(String key, String gatePassId, String kioskId, String correlationId) {
    long started = System.nanoTime();
    IntegrationConfiguration config = configurations.required(key);
    if (!config.isEnabled() || config.getBaseUrl() == null || config.getBaseUrl().isBlank()) {
      return failed(key, gatePassId, "CONFIGURATION_ERROR", "Integration is not available");
    }
    JsonNode response = null;
    Integer httpStatus = null;
    RuntimeException lastFailure = null;
    for (int attempt = 0; attempt <= config.getRetryCount(); attempt++) {
      try {
        IntegrationWorkflowExecutor.Execution execution = workflowExecutor.execute(config, gatePassId, kioskId);
        response = execution.response();
        httpStatus = execution.httpStatus();
        break;
      } catch (RuntimeException exception) {
        lastFailure = exception;
      }
    }
    if (response == null) throw lastFailure == null ? new IllegalStateException("Empty integration response") : lastFailure;
    if ("TASREEH".equals(key)) {
      return normalizeTasreeh(response, httpStatus, correlationId, gatePassId, started);
    }
    JsonNode approvalNode = resolve(response, config.getApprovalField());
    boolean approved = "$httpStatus".equals(config.getApprovalField())
        ? config.getApprovalValue().equals(String.valueOf(httpStatus))
        : approvalNode != null && config.getApprovalValue().equalsIgnoreCase(approvalNode.asText());
    String outcome = approved ? "APPROVED" : "REJECTED";
    recordExecution(correlationId, gatePassId, key, outcome, httpStatus, started);
    return new NormalizedVerificationResult(key, approved, outcome,
        approved ? "Gate Pass verified" : "Gate Pass rejected", gatePassId,
        text(response, "applicationId"), text(response, "visitorName"), null, Map.of());
  }

  private NormalizedVerificationResult normalizeTasreeh(JsonNode response, Integer httpStatus, String correlationId,
      String gatePassId, long started) {
    TasreehKioskPassResponse envelope = mapper.convertValue(response, TasreehKioskPassResponse.class);
    if (envelope == null) envelope = new TasreehKioskPassResponse(null, java.util.List.of(), null, null, null);
    var errors = envelope.errors() == null ? java.util.List.<TasreehKioskPassResponse.TasreehError>of()
        : envelope.errors().stream().filter(java.util.Objects::nonNull).toList();
    boolean approved = httpStatus != null && httpStatus >= 200 && httpStatus < 300
        && "SUCCESS".equalsIgnoreCase(envelope.status())
        && Integer.valueOf(200).equals(envelope.statusCode())
        && errors.isEmpty();
    String outcome = approved ? "APPROVED" : "REJECTED";
    String providerMessage = firstTasreehError(errors, envelope.message(), approved);
    if (!approved && !errors.isEmpty()) {
      logger.warn("Tasreeh rejected pass: errorCode={}, errorMessage={}", errors.getFirst().errCode(), errors.getFirst().errMsg());
    }
    recordExecution(correlationId, gatePassId, "TASREEH", outcome, httpStatus, started);
    Map<String, Object> metadata = mapper.convertValue(envelope,
        new com.fasterxml.jackson.core.type.TypeReference<LinkedHashMap<String, Object>>() {});
    String visitorName = envelope.data() == null ? null : envelope.data().visitorName();
    return new NormalizedVerificationResult("TASREEH", approved,
        envelope.status() == null ? outcome : envelope.status(), providerMessage, gatePassId,
        null, visitorName, null, metadata);
  }

  private String firstTasreehError(java.util.List<TasreehKioskPassResponse.TasreehError> errors,
      String envelopeMessage, boolean approved) {
    if (!errors.isEmpty() && errors.getFirst().errMsg() != null && !errors.getFirst().errMsg().isBlank()) {
      return errors.getFirst().errMsg();
    }
    if (envelopeMessage != null && !envelopeMessage.isBlank()) return envelopeMessage;
    return approved ? "Gate Pass verified" : "Tasreeh rejected the gate pass";
  }

  private void recordExecution(String correlationId, String gatePassId, String key, String outcome, Integer httpStatus, long started) {
    VerificationExecutionLog log = new VerificationExecutionLog();
    log.setCorrelationId(correlationId); log.setGatePassHash(hash(gatePassId)); log.setIntegrationKey(key);
    log.setOutcome(outcome); log.setHttpStatus(httpStatus); log.setDurationMs((System.nanoTime() - started) / 1_000_000);
    try {
      logs.save(log);
    } catch (DataAccessException exception) {
      // Verification must not be converted into a provider outage by an optional audit write.
      logger.error("Unable to persist {} verification execution log; check Flyway migration status", key, exception);
    }
  }

  private String hash(String value) {
    try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
    catch (Exception exception) { throw new IllegalStateException("Cannot sanitize verification identifier", exception); }
  }

  private NormalizedVerificationResult join(CompletableFuture<NormalizedVerificationResult> future, String key, String gatePassId) {
    try { return future.join(); }
    catch (CompletionException exception) { return failed(key, gatePassId, "SYSTEM_ERROR", "Integration is temporarily unavailable"); }
  }

  private boolean isUnavailable(NormalizedVerificationResult result) {
    return "SYSTEM_ERROR".equals(result.status()) || "CONFIGURATION_ERROR".equals(result.status());
  }

  private NormalizedVerificationResult failed(String key, String gatePassId, String status, String message) {
    return new NormalizedVerificationResult(key, false, status, message, gatePassId, null, null, (OffsetDateTime) null, Map.of());
  }

  private String text(JsonNode node, String field) {
    JsonNode value = resolve(node, field);
    return value == null || value.isNull() ? null : value.asText();
  }

  static JsonNode resolve(JsonNode node, String path) {
    if (node == null || path == null || path.isBlank()) return node;
    JsonNode current = node;
    for (String segment : path.replaceAll("\\[(\\d+)]", ".$1").split("\\.")) {
      current = segment.matches("\\d+") ? current.path(Integer.parseInt(segment)) : current.path(segment);
      if (current.isMissingNode()) return null;
    }
    return current;
  }
}
