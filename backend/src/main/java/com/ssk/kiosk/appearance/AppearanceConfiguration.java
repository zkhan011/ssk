package com.ssk.kiosk.appearance;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
public class AppearanceConfiguration {
  @Id private UUID id = UUID.randomUUID();
  @Enumerated(EnumType.STRING) private Status status;
  private long versionNumber;
  private Instant createdAt = Instant.now();
  private Instant publishedAt;
  private String changedBy;
  @Lob private String snapshot;
  @Version private Long rowVersion;
  public enum Status { DRAFT, PUBLISHED, ARCHIVED }
  public UUID getId(){return id;} public Status getStatus(){return status;} public void setStatus(Status value){status=value;}
  public long getVersionNumber(){return versionNumber;} public void setVersionNumber(long value){versionNumber=value;}
  public Instant getCreatedAt(){return createdAt;} public Instant getPublishedAt(){return publishedAt;} public void setPublishedAt(Instant value){publishedAt=value;}
  public String getChangedBy(){return changedBy;} public void setChangedBy(String value){changedBy=value;}
  public String getSnapshot(){return snapshot;} public void setSnapshot(String value){snapshot=value;}
}
