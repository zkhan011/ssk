package com.ssk.kiosk.appearance;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
public class AppearanceMedia {
  @Id private UUID id = UUID.randomUUID();
  private String storedName;
  private String contentType;
  private long sizeBytes;
  private String checksum;
  private Instant createdAt = Instant.now();
  private String createdBy;
  @Version private Long rowVersion;
  public UUID getId(){return id;} public String getStoredName(){return storedName;} public void setStoredName(String v){storedName=v;} public String getContentType(){return contentType;} public void setContentType(String v){contentType=v;} public long getSizeBytes(){return sizeBytes;} public void setSizeBytes(long v){sizeBytes=v;} public String getChecksum(){return checksum;} public void setChecksum(String v){checksum=v;} public Instant getCreatedAt(){return createdAt;} public String getCreatedBy(){return createdBy;} public void setCreatedBy(String v){createdBy=v;}
}
