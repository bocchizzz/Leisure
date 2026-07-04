package com.bangboo.adminops.service;

import com.bangboo.adminops.client.InternalStatsClient;
import com.bangboo.adminops.dto.AiStatsVO;
import com.bangboo.adminops.dto.DashboardStats;
import com.bangboo.adminops.dto.IamStatsVO;
import com.bangboo.adminops.dto.MarketplaceStatsVO;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final InternalStatsClient internalStatsClient;

    public DashboardService(InternalStatsClient internalStatsClient) {
        this.internalStatsClient = internalStatsClient;
    }

    public DashboardStats stats() {
        IamStatsVO iam = internalStatsClient.iamStats();
        MarketplaceStatsVO marketplace = internalStatsClient.marketplaceStats();
        AiStatsVO ai = internalStatsClient.aiStats();
        return new DashboardStats(
                iam.userCount(),
                iam.certifiedUserCount(),
                marketplace.taskCount(),
                marketplace.recruitingTaskCount(),
                marketplace.completedTaskCount(),
                marketplace.disputeTaskCount(),
                ai.aiCallCount()
        );
    }
}
