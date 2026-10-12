package com.ssk.kiosk.integration;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verification_execution_log", schema = "dbo")
public class VerificationExecutionLog {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  private String correlationId;
  private String gatePassHash;
  private String integrationKey;
  private String outcome;
  private Integer httpStatus;
  private long durationMs;
  private Instant createdAt = Instant.now();
  public void setCorrelationId(String v){correlationId=v;} public void setGatePassHash(String v){gatePassHash=v;} public void setIntegrationKey(String v){integrationKey=v;} public void setOutcome(String v){outcome=v;} public void setHttpStatus(Integer v){httpStatus=v;} public void setDurationMs(long v){durationMs=v;}
}
