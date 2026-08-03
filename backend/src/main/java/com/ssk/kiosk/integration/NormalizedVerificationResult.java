package com.ssk.kiosk.integration;

import java.time.OffsetDateTime;
import java.util.Map;

public record NormalizedVerificationResult(
    String integration,
    boolean verified,
    String status,
    String message,
    String gatePassId,
    String applicationId,
    String visitorName,
    OffsetDateTime validUntil,
    Map<String, Object> metadata) {
}
