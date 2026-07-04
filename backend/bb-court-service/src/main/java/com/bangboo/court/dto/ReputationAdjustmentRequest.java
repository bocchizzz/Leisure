package com.bangboo.court.dto;

public record ReputationAdjustmentRequest(
        int delta,
        String reason,
        String sourceType,
        Long sourceId
) {
}
