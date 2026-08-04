package com.ssk.kiosk.flow;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
public class ScreenFlowConfiguration {
  @Id private UUID id = UUID.randomUUID();
  @Enumerated(EnumType.STRING) private Status status;
  private long versionNumber;
  @Lob private String snapshot;
  private Instant createdAt = Instant.now();
  private Instant publishedAt;
  private String changedBy;
  @Version private Long rowVersion;
  public enum Status { DRAFT, PUBLISHED, ARCHIVED }
  public Status getStatus(){return status;} public void setStatus(Status v){status=v;} public long getVersionNumber(){return versionNumber;} public void setVersionNumber(long v){versionNumber=v;} public String getSnapshot(){return snapshot;} public void setSnapshot(String v){snapshot=v;} public Instant getPublishedAt(){return publishedAt;} public void setPublishedAt(Instant v){publishedAt=v;} public void setChangedBy(String v){changedBy=v;}
}
