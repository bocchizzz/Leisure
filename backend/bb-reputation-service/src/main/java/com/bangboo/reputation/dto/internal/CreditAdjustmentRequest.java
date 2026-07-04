package com.bangboo.reputation.dto.internal;

/**
 * 信誉变更请求（POST /internal/credit-adjustments 入参）。
 * 字段与 marketplace/court 调用时发送的 body 一致：userId/delta/sourceType/sourceId/reason。
 */
public record CreditAdjustmentRequest(
        Long userId,
        Integer delta,
        String sourceType,
        Long sourceId,
        String reason
) {
}
