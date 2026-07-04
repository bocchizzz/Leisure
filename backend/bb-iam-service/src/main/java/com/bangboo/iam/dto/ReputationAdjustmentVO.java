package com.bangboo.iam.dto;

public record ReputationAdjustmentVO(
        Long userId,
        int beforeScore,
        int afterScore
) {
}
