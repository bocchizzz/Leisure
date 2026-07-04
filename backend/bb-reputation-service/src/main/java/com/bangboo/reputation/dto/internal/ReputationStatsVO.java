package com.bangboo.reputation.dto.internal;

/** reputation 统计（GET /internal/stats/reputation），供 admin-ops。 */
public record ReputationStatsVO(
        long reviewCount,
        long creditLogCount
) {
}
