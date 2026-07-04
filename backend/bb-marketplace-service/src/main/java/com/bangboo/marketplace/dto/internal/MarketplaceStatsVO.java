package com.bangboo.marketplace.dto.internal;

/** marketplace 统计，供 admin-ops dashboard 聚合。字段对齐 mock-b-services /internal/stats/marketplace。 */
public record MarketplaceStatsVO(
        long taskCount,
        long recruitingTaskCount,
        long completedTaskCount,
        long contractCount,
        long disputeTaskCount
) {
}
