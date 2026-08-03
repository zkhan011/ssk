package com.ssk.kiosk.integration;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VerificationExecutionLogRepository extends JpaRepository<VerificationExecutionLog, UUID> {}
