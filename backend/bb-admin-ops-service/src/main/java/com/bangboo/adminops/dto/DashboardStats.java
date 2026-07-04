package com.bangboo.adminops.dto;

public record DashboardStats(
        long userCount,
        long certifiedUserCount,
        long taskCount,
        long recruitingTaskCount,
        long completedTaskCount,
        long disputeCount,
        long aiCallCount
) {
}
