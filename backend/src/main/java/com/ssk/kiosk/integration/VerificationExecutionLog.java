package com.ssk.kiosk.integration;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Entity
public class VerificationExecutionLog {
  @Id private UUID id = UUID.randomUUID();
  private String correlationId;
  private String gatePassHash;
  private String integrationKey;
  private String outcome;
  private Integer httpStatus;
  private long durationMs;
  private Instant createdAt = Instant.now();
  public void setCorrelationId(String v){correlationId=v;} public void setGatePassHash(String v){gatePassHash=v;} public void setIntegrationKey(String v){integrationKey=v;} public void setOutcome(String v){outcome=v;} public void setHttpStatus(Integer v){httpStatus=v;} public void setDurationMs(long v){durationMs=v;}
}
