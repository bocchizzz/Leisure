package com.campus.market.notify.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.notify.enums.AppealStatus;
import com.campus.market.notify.repository.AppealRepository;
import com.campus.market.notify.repository.AuditLogRepository;
import com.campus.market.notify.repository.MessageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台仪表盘控制器
 * 注：微服务架构下，完整统计需要前端分别调用各服务的stats接口
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理后台仪表盘", description = "统计数据接口")
public class DashboardController {
    
    private final AppealRepository appealRepository;
    private final AuditLogRepository auditLogRepository;
    private final MessageRepository messageRepository;
    
    @GetMapping("/dashboard")
    @Operation(summary = "获取仪表盘统计数据")
    public ApiResponse<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 本地申诉统计
        stats.put("pendingAppeals", appealRepository.countByStatus(AppealStatus.PENDING));
        stats.put("totalAppeals", appealRepository.count());
        
        // 审计日志统计
        stats.put("totalAuditLogs", auditLogRepository.count());
        
        // 消息统计
        stats.put("totalMessages", messageRepository.count());
        
        return ApiResponse.success(stats);
    }
}
