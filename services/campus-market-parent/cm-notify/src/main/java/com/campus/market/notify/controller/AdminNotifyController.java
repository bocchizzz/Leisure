package com.campus.market.notify.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.notify.entity.Appeal;
import com.campus.market.notify.entity.AuditLog;
import com.campus.market.notify.enums.AppealStatus;
import com.campus.market.notify.service.AppealService;
import com.campus.market.notify.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员通知控制器
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理员通知接口", description = "申诉处理和审计日志")
public class AdminNotifyController {
    
    private final AppealService appealService;
    private final AuditLogService auditLogService;
    
    // ========== 申诉管理 ==========
    
    @GetMapping("/appeals")
    @Operation(summary = "申诉列表")
    public ApiResponse<Page<Appeal>> listAppeals(
            @RequestParam(required = false) AppealStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Appeal> appeals = appealService.listAppeals(status, pageable);
        return ApiResponse.success(appeals);
    }
    
    @PutMapping({"/appeals/{id}", "/appeals/{id}/process"})
    @Operation(summary = "处理申诉")
    public ApiResponse<Appeal> processAppeal(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String response) {
        // 兼容E4的Query参数方式和E5的Body方式
        AppealStatus appealStatus;
        String responseText;
        
        if (body != null && body.containsKey("status")) {
            appealStatus = AppealStatus.valueOf((String) body.get("status"));
            responseText = (String) body.get("response");
        } else {
            appealStatus = AppealStatus.valueOf(status);
            responseText = response;
        }
        
        Appeal appeal = appealService.processAppeal(id, appealStatus, responseText);
        return ApiResponse.success("处理成功", appeal);
    }
    
    // ========== 审计日志 ==========
    
    @GetMapping("/audit-logs")
    @Operation(summary = "审计日志列表")
    public ApiResponse<Page<AuditLog>> listAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditLog> logs = auditLogService.listLogs(action, username, pageable);
        return ApiResponse.success(logs);
    }
}
