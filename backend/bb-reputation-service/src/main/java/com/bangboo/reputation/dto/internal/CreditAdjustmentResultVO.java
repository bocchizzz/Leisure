package com.bangboo.reputation.dto.internal;

/** 信誉变更返回：变更前后分值。 */
public record CreditAdjustmentResultVO(
        Long userId,
        Integer beforeScore,
        Integer afterScore
) {
}
