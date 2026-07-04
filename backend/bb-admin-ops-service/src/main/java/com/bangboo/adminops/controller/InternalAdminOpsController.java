package com.bangboo.adminops.controller;

import com.bangboo.adminops.dto.AuditLogVO;
import com.bangboo.adminops.dto.CreateAuditLogRequest;
import com.bangboo.adminops.dto.OpsConfigVO;
import com.bangboo.adminops.service.AuditLogService;
import com.bangboo.adminops.service.OpsConfigService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class InternalAdminOpsController {
    private final AuditLogService auditLogService;
    private final OpsConfigService opsConfigService;

    public InternalAdminOpsController(AuditLogService auditLogService, OpsConfigService opsConfigService) {
        this.auditLogService = auditLogService;
        this.opsConfigService = opsConfigService;
    }

    @PostMapping("/audit-logs")
    public AuditLogVO createAuditLog(@Valid @RequestBody CreateAuditLogRequest request) {
        return auditLogService.create(request);
    }

    @GetMapping("/ops-config")
    public OpsConfigVO internalOpsConfig() {
        return opsConfigService.get();
    }
}
