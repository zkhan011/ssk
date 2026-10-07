package com.ssk.kiosk.appearance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AppearanceServiceTest {
  @Test
  void providesBodyAppearanceAndResultPageDefaults() {
    Map<String, Object> defaults = new AppearanceService(
        mock(AppearanceConfigurationRepository.class), new ObjectMapper()).defaults();

    assertEquals("cover", defaults.get("bodyBackgroundSize"));
    assertEquals("#f7f7f8", defaults.get("bodyBackgroundColor"));
    Map<?, ?> headerColors = (Map<?, ?>) defaults.get("headerColors");
    assertTrue(headerColors.containsKey("accessDenied"));
    assertTrue(headerColors.containsKey("somethingWentWrong"));
  }
}
