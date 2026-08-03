package com.ssk.kiosk.integration;

import java.util.Map;

public record GatePassVerificationResponse(
    String gatePassId,
    String outcome,
    String message,
    Map<String, NormalizedVerificationResult> integrations) {
}
