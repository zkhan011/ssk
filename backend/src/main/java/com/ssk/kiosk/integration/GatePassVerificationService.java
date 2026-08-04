package com.ssk.kiosk.integration;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GatePassVerificationService {
  private final IntegrationConfigurationService configurations;
  private final VerificationExecutionLogRepository logs;
  private final IntegrationWorkflowExecutor workflowExecutor;

  public GatePassVerificationService(IntegrationConfigurationService configurations, VerificationExecutionLogRepository logs, IntegrationWorkflowExecutor workflowExecutor) {
    this.configurations = configurations;
    this.logs = logs;
    this.workflowExecutor = workflowExecutor;
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
    String message = "APPROVED".equals(outcome) ? "Gate Pass verified" : "Gate Pass verification was not approved";
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

  private void recordExecution(String correlationId, String gatePassId, String key, String outcome, Integer httpStatus, long started) {
    VerificationExecutionLog log = new VerificationExecutionLog();
    log.setCorrelationId(correlationId); log.setGatePassHash(hash(gatePassId)); log.setIntegrationKey(key);
    log.setOutcome(outcome); log.setHttpStatus(httpStatus); log.setDurationMs((System.nanoTime() - started) / 1_000_000);
    logs.save(log);
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
