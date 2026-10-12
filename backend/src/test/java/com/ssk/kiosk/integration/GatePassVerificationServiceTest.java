package com.ssk.kiosk.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataAccessResourceFailureException;

class GatePassVerificationServiceTest {
  private HttpServer server;
  private IntegrationConfigurationService configurations;
  private AtomicInteger tasreehCalls;
  private AtomicInteger panguCalls;
  private AtomicReference<String> tasreehResponse;

  @BeforeEach
  void startServer() throws Exception {
    tasreehCalls = new AtomicInteger();
    panguCalls = new AtomicInteger();
    tasreehResponse = new AtomicReference<>("""
        {"data":{"company":"Redacted Co","date":"2026-01-02","emiratesIdNumber":"XXX-0000",
        "hostNameAndApproverDepartment":"Test Host / QA","idProof":"REDACTED","location":"Test Port",
        "nationality":"Test","passValidityDate":"2026-01-03","passportNumber":"P-REDACTED",
        "photo":"data:image/jpeg;base64,AA==","purposeOfVisit":"Testing","specificLocation":"Test Gate",
        "typeOfPass":"Visitor","vehiclePassNumber":"VEH-1","visitorName":"Synthetic Visitor","visitorType":"Guest"},
        "errors":[],"message":"Request Success","status":"SUCCESS","statusCode":200}
        """);
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/tasreeh", exchange -> respond(exchange, tasreehCalls, tasreehResponse.get()));
    endpoint("/pangu", panguCalls, false);
    server.start();
    configurations = Mockito.mock(IntegrationConfigurationService.class);
    when(configurations.required("TASREEH")).thenReturn(configuration("/tasreeh"));
    when(configurations.required("PANGU")).thenReturn(configuration("/pangu"));
    when(configurations.workflow(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> { IntegrationConfiguration c=invocation.getArgument(0); return java.util.List.of(java.util.Map.of("id","verify","method","POST","path",c.getVerificationPath(),"body",java.util.Map.of("gatePassId","{{input.gatePassId}}"),"successStatusCodes",java.util.List.of(200),"outputs",java.util.Map.of())); });
  }

  @AfterEach void stopServer() { server.stop(0); }

  @Test
  void requiresApprovalFromBothIntegrations() {
    GatePassVerificationResponse result = new GatePassVerificationService(configurations,
        mock(VerificationExecutionLogRepository.class), new IntegrationWorkflowExecutor(configurations, new com.fasterxml.jackson.databind.ObjectMapper(), new org.springframework.mock.env.MockEnvironment()), new com.fasterxml.jackson.databind.ObjectMapper()).verify("ABC-123", "KIOSK-1");
    assertEquals("REJECTED", result.outcome());
    assertEquals(1, tasreehCalls.get());
    assertEquals(1, panguCalls.get());
    assertFalse(result.integrations().get("PANGU").verified());
    NormalizedVerificationResult tasreeh = result.integrations().get("TASREEH");
    assertEquals("Synthetic Visitor", tasreeh.visitorName());
    assertEquals("SUCCESS", tasreeh.status());
    var data = (java.util.Map<?, ?>) tasreeh.metadata().get("data");
    assertEquals(java.util.Set.of("company", "date", "emiratesIdNumber", "hostNameAndApproverDepartment",
        "idProof", "location", "nationality", "passValidityDate", "passportNumber", "photo",
        "purposeOfVisit", "specificLocation", "typeOfPass", "vehiclePassNumber", "visitorName", "visitorType"),
        data.keySet());
    assertEquals("data:image/jpeg;base64,AA==", data.get("photo"));
    assertEquals(200, tasreeh.metadata().get("statusCode"));
  }

  @Test
  void preservesTasreehApplicationErrorAndDoesNotApproveHttp200Failure() {
    tasreehResponse.set("""
        {"data":{},"errors":[{"errCode":300003,"errMsg":"Invalid Data: Pass Reference Number"}],
        "message":null,"status":"FAILED","statusCode":null}
        """);

    GatePassVerificationResponse result = new GatePassVerificationService(configurations,
        mock(VerificationExecutionLogRepository.class),
        new IntegrationWorkflowExecutor(configurations, new com.fasterxml.jackson.databind.ObjectMapper(), new org.springframework.mock.env.MockEnvironment()),
        new com.fasterxml.jackson.databind.ObjectMapper()).verify("REF-INVALID", "KIOSK-1");

    NormalizedVerificationResult tasreeh = result.integrations().get("TASREEH");
    assertFalse(tasreeh.verified());
    assertEquals("FAILED", tasreeh.status());
    assertEquals("Invalid Data: Pass Reference Number", tasreeh.message());
    assertEquals("Invalid Data: Pass Reference Number", result.message());
    assertEquals("FAILED", tasreeh.metadata().get("status"));
    assertNull(tasreeh.metadata().get("statusCode"));
    assertNull(tasreeh.metadata().get("message"));
    var errors = (java.util.List<?>) tasreeh.metadata().get("errors");
    assertEquals(300003, ((java.util.Map<?, ?>) errors.getFirst()).get("errCode"));
    assertEquals("Invalid Data: Pass Reference Number", ((java.util.Map<?, ?>) errors.getFirst()).get("errMsg"));
  }

  @Test
  void resolvesNestedAndIndexedResponseFields() throws Exception {
    var json = new com.fasterxml.jackson.databind.ObjectMapper().readTree("{\"items\":[{\"result\":{\"approved\":true}}]}");
    assertNotNull(GatePassVerificationService.resolve(json, "items[0].result.approved"));
  }

  @Test
  void verificationResultSurvivesAuditStorageFailure() {
    VerificationExecutionLogRepository unavailableLogs = mock(VerificationExecutionLogRepository.class);
    doThrow(new DataAccessResourceFailureException("audit database unavailable"))
        .when(unavailableLogs).save(org.mockito.ArgumentMatchers.any(VerificationExecutionLog.class));

    GatePassVerificationResponse result = new GatePassVerificationService(configurations, unavailableLogs,
        new IntegrationWorkflowExecutor(configurations, new com.fasterxml.jackson.databind.ObjectMapper(), new org.springframework.mock.env.MockEnvironment()), new com.fasterxml.jackson.databind.ObjectMapper())
        .verify("ABC-123", "KIOSK-1");

    assertEquals("REJECTED", result.outcome());
  }

  private IntegrationConfiguration configuration(String path) {
    IntegrationConfiguration value = new IntegrationConfiguration();
    value.setEnabled(true);
    value.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
    value.setVerificationPath(path);
    value.setApprovalField("data.approved");
    value.setApprovalValue("true");
    value.setConnectTimeoutMs(1000);
    value.setReadTimeoutMs(1000);
    return value;
  }

  private void endpoint(String path, AtomicInteger calls, boolean approved) {
    server.createContext(path, exchange -> {
      respond(exchange, calls, "{\"data\":{\"approved\":" + approved + "}}");
    });
  }

  private void respond(com.sun.net.httpserver.HttpExchange exchange, AtomicInteger calls, String json) throws java.io.IOException {
    calls.incrementAndGet();
    byte[] response = json.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.sendResponseHeaders(200, response.length);
    exchange.getResponseBody().write(response);
    exchange.close();
  }
}
