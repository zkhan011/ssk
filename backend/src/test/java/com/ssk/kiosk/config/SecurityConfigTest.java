package com.ssk.kiosk.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class SecurityConfigTest {
  @Test
  void corsAllowsBothViteDevelopmentOriginsAndPreflight() {
    var source = new SecurityConfig().corsConfigurationSource(
        "http://localhost:5173,http://127.0.0.1:5173");
    var configuration = source.getCorsConfiguration(
        new MockHttpServletRequest("OPTIONS", "/api/v1/verification/gate-pass"));
    assertTrue(configuration.getAllowedOrigins().contains("http://127.0.0.1:5173"));
    assertTrue(configuration.getAllowedMethods().contains("OPTIONS"));
    assertEquals(Boolean.TRUE, configuration.getAllowCredentials());
  }
}
