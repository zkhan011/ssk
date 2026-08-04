package com.ssk.kiosk.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class GatePassVerificationServiceTest {
  private HttpServer server;
  private IntegrationConfigurationService configurations;
  private AtomicInteger tasreehCalls;
  private AtomicInteger panguCalls;

  @BeforeEach
  void startServer() throws Exception {
    tasreehCalls = new AtomicInteger();
    panguCalls = new AtomicInteger();
    server = HttpServer.create(new InetSocketAddress(0), 0);
    endpoint("/tasreeh", tasreehCalls, true);
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
        mock(VerificationExecutionLogRepository.class), new IntegrationWorkflowExecutor(configurations, new com.fasterxml.jackson.databind.ObjectMapper(), new org.springframework.mock.env.MockEnvironment())).verify("ABC-123", "KIOSK-1");
    assertEquals("REJECTED", result.outcome());
    assertEquals(1, tasreehCalls.get());
    assertEquals(1, panguCalls.get());
    assertFalse(result.integrations().get("PANGU").verified());
  }

  @Test
  void resolvesNestedAndIndexedResponseFields() throws Exception {
    var json = new com.fasterxml.jackson.databind.ObjectMapper().readTree("{\"items\":[{\"result\":{\"approved\":true}}]}");
    assertNotNull(GatePassVerificationService.resolve(json, "items[0].result.approved"));
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
      calls.incrementAndGet();
      byte[] response = ("{\"data\":{\"approved\":" + approved + "}}").getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, response.length);
      exchange.getResponseBody().write(response);
      exchange.close();
    });
  }
}
