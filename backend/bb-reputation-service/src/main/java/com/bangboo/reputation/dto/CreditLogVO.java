package com.bangboo.reputation.dto;

import java.time.Instant;

/** 信誉变化日志视图对象。字段对齐前端 user.ts CreditLogVO。 */
public record CreditLogVO(
        Long id,
        Long userId,
        Integer delta,
        Integer beforeScore,
        Integer afterScore,
        String sourceType,
        Long sourceId,
        String reason,
        Instant createdAt
) {
}
