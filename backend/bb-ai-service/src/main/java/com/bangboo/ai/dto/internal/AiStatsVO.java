package com.bangboo.ai.dto.internal;

/** AI 调用统计（GET /internal/stats/ai），供 admin-ops。 */
public record AiStatsVO(
        long aiCallCount
) {
}
