package com.bangboo.adminops.controller;

import com.bangboo.adminops.dto.AuditLogVO;
import com.bangboo.adminops.dto.DashboardStats;
import com.bangboo.adminops.dto.OpsConfigVO;
import com.bangboo.adminops.service.AdminAuthService;
import com.bangboo.adminops.service.AuditLogService;
import com.bangboo.adminops.service.DashboardService;
import com.bangboo.adminops.service.OpsConfigService;
import com.bangboo.common.response.PageResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminOpsController {
    private final AdminAuthService adminAuthService;
    private final DashboardService dashboardService;
    private final OpsConfigService opsConfigService;
    private final AuditLogService auditLogService;

    public AdminOpsController(
            AdminAuthService adminAuthService,
            DashboardService dashboardService,
            OpsConfigService opsConfigService,
            AuditLogService auditLogService
    ) {
        this.adminAuthService = adminAuthService;
        this.dashboardService = dashboardService;
        this.opsConfigService = opsConfigService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/dashboard")
    public DashboardStats dashboard() {
        adminAuthService.requireAdmin();
        return dashboardService.stats();
    }

    @GetMapping("/ops-config")
    public OpsConfigVO opsConfig() {
        adminAuthService.requireAdmin();
        return opsConfigService.get();
    }

    @PutMapping("/ops-config")
    public OpsConfigVO updateOpsConfig(@RequestBody OpsConfigVO request) {
        adminAuthService.requireAdmin();
        return opsConfigService.update(request);
    }

    @GetMapping("/audit-logs")
    public PageResult<AuditLogVO> auditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        adminAuthService.requireAdmin();
        return auditLogService.list(page, size);
    }
}
