package com.bangboo.adminops.dto;

public record MarketplaceStatsVO(
        long taskCount,
        long recruitingTaskCount,
        long completedTaskCount,
        long contractCount,
        long disputeTaskCount
) {
}
