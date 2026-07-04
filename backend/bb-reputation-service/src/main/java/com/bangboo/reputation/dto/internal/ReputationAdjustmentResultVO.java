package com.bangboo.reputation.dto.internal;

/** IAM 信誉分调整返回（POST /internal/users/{id}/reputation-adjustments）。 */
public record ReputationAdjustmentResultVO(
        Long userId,
        Integer beforeScore,
        Integer afterScore
) {
}
