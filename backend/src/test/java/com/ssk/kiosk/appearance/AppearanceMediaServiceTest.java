package com.ssk.kiosk.appearance;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class AppearanceMediaServiceTest {
  @TempDir Path directory;

  @Test
  void rejectsExecutableSvgContent() throws Exception {
    AppearanceMediaService service = new AppearanceMediaService(
        mock(AppearanceMediaRepository.class), directory.toString(), 5_242_880);
    var file = new MockMultipartFile("file", "logo.svg", "image/svg+xml",
        "<svg onload=\"alert(1)\"></svg>".getBytes(StandardCharsets.UTF_8));
    assertThrows(IllegalArgumentException.class, () -> service.store(file, "admin"));
  }

  @Test
  void rejectsFakeImageContentRegardlessOfClaimedMimeType() throws Exception {
    AppearanceMediaService service = new AppearanceMediaService(
        mock(AppearanceMediaRepository.class), directory.toString(), 5_242_880);
    var file = new MockMultipartFile("file", "logo.png", "image/png", "not an image".getBytes(StandardCharsets.UTF_8));
    assertThrows(IllegalArgumentException.class, () -> service.store(file, "admin"));
  }
}
