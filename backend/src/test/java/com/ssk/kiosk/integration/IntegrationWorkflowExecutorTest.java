package com.ssk.kiosk.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class IntegrationWorkflowExecutorTest {
  private HttpServer server;
  @AfterEach void stop(){if(server!=null)server.stop(0);}

  @Test
  void passesAuthenticationOutputIntoPushHeaderAndRendersGatePassPayload() throws Exception {
    AtomicReference<String> authorization=new AtomicReference<>();AtomicReference<String> payload=new AtomicReference<>();AtomicReference<String> tokenPayload=new AtomicReference<>();AtomicReference<String> tokenContentType=new AtomicReference<>();
    server=HttpServer.create(new InetSocketAddress(0),0);
    server.createContext("/auth",exchange->{tokenPayload.set(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));tokenContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));byte[] value="{\"access_token\":\"token-123\"}".getBytes(StandardCharsets.UTF_8);exchange.getResponseHeaders().add("Content-Type","application/json");exchange.sendResponseHeaders(200,value.length);exchange.getResponseBody().write(value);exchange.close();});
    server.createContext("/sync",exchange->{authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));payload.set(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));byte[] value="{\"result\":\"Successful\"}".getBytes(StandardCharsets.UTF_8);exchange.getResponseHeaders().add("Content-Type","application/json");exchange.sendResponseHeaders(200,value.length);exchange.getResponseBody().write(value);exchange.close();});server.start();
    IntegrationConfiguration config=new IntegrationConfiguration();config.setBaseUrl("http://127.0.0.1:"+server.getAddress().getPort());config.setConnectTimeoutMs(1000);config.setReadTimeoutMs(1000);config.setRetryCount(0);
    IntegrationConfigurationService configurations=org.mockito.Mockito.mock(IntegrationConfigurationService.class);
    when(configurations.workflow(config)).thenReturn(List.of(
        Map.of("id","authenticate","method","POST","baseUrl","http://127.0.0.1:"+server.getAddress().getPort(),"path","/auth","contentType","application/x-www-form-urlencoded","body",Map.of("username","{{secrets.TASREEH_USERNAME}}","grant_type","password"),"successStatusCodes",List.of(200),"outputs",Map.of("accessToken","access_token")),
        Map.of("id","employeeAccessSync","method","POST","path","/sync","headers",Map.of("Authorization","Bearer {{steps.authenticate.outputs.accessToken}}"),"body",Map.of("gatePassId","{{input.gatePassId}}"),"successStatusCodes",List.of(200),"outputs",Map.of())));
    var result=new IntegrationWorkflowExecutor(configurations,new ObjectMapper(),new MockEnvironment().withProperty("TASREEH_USERNAME","kiosk user")).execute(config,"ABC-123","KIOSK-1");
    assertEquals(200,result.httpStatus());assertTrue(tokenContentType.get().startsWith("application/x-www-form-urlencoded"));assertTrue(tokenPayload.get().contains("grant_type=password"));assertTrue(tokenPayload.get().contains("username=kiosk+user"));assertEquals("Bearer token-123",authorization.get());assertEquals("ABC-123",new ObjectMapper().readTree(payload.get()).path("gatePassId").asText());
  }
}
