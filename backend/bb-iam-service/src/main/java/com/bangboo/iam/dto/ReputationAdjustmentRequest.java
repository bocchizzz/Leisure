package com.bangboo.iam.dto;

import jakarta.validation.constraints.NotNull;

public record ReputationAdjustmentRequest(
        int delta,
        @NotNull String sourceType,
        Long sourceId,
        String reason
) {
}
