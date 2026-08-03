package com.ssk.kiosk.config;

import com.ssk.kiosk.appearance.AppearanceMedia;
import com.ssk.kiosk.appearance.AppearanceMediaService;
import java.io.IOException;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StaticResourceConfig {
  private final AppearanceMediaService media;
  public StaticResourceConfig(AppearanceMediaService media){this.media=media;}
  @GetMapping("/uploads/appearance/{id}")
  ResponseEntity<FileSystemResource> image(@PathVariable UUID id) throws IOException {
    AppearanceMedia metadata=media.metadata(id);
    return ResponseEntity.ok().cacheControl(CacheControl.noCache()).header("Content-Type",metadata.getContentType())
        .body(new FileSystemResource(media.resolve(id)));
  }
}
