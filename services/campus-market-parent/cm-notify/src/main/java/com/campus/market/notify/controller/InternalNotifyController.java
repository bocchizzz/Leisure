package com.campus.market.notify.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.notify.entity.Appeal;
import com.campus.market.notify.enums.MessageType;
import com.campus.market.notify.service.AppealService;
import com.campus.market.notify.service.AuditLogService;
import com.campus.market.notify.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 内部通知接口（供其他微服务调用）
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Tag(name = "内部通知接口", description = "供其他微服务调用")
public class InternalNotifyController {
    
    private final MessageService messageService;
    private final AuditLogService auditLogService;
    private final AppealService appealService;
    
    @PostMapping("/messages")
    @Operation(summary = "发送消息")
    public ApiResponse<Void> sendMessage(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        MessageType type = MessageType.valueOf((String) request.get("type"));
        String title = (String) request.get("title");
        String content = (String) request.get("content");
        Long relatedId = request.get("relatedId") != null ? 
                Long.valueOf(request.get("relatedId").toString()) : null;
        
        messageService.sendMessage(userId, type, title, content, relatedId);
        return ApiResponse.success(null);
    }
    
    @PostMapping("/audit-logs")
    @Operation(summary = "记录审计日志")
    public ApiResponse<Void> recordAuditLog(@RequestBody Map<String, Object> request) {
        Long userId = request.get("userId") != null ? 
                Long.valueOf(request.get("userId").toString()) : null;
        String username = (String) request.get("username");
        String action = (String) request.get("action");
        String method = (String) request.get("method");
        String params = (String) request.get("params");
        String ip = (String) request.get("ip");
        Long costTime = request.get("costTime") != null ? 
                Long.valueOf(request.get("costTime").toString()) : null;
        String status = (String) request.get("status");
        String errorMsg = (String) request.get("errorMsg");
        
        auditLogService.recordLog(userId, username, action, method, params, ip, costTime, status, errorMsg);
        return ApiResponse.success(null);
    }
    
    @GetMapping("/appeals/all")
    @Operation(summary = "获取所有申诉列表（供AI使用）")
    public ApiResponse<List<Map<String, Object>>> getAllAppeals() {
        Page<Appeal> appeals = appealService.listAppeals(null, PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<Map<String, Object>> result = appeals.getContent().stream()
                .map(appeal -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", appeal.getId());
                    info.put("userId", appeal.getUserId());
                    info.put("type", appeal.getType());
                    info.put("content", appeal.getContent());
                    info.put("status", appeal.getStatus().name());
                    info.put("statusName", getAppealStatusName(appeal.getStatus().name()));
                    info.put("createdAt", appeal.getCreatedAt());
                    return info;
                })
                .collect(Collectors.toList());
        return ApiResponse.success(result);
    }
    
    private String getAppealStatusName(String status) {
        switch (status) {
            case "PENDING": return "待处理";
            case "PROCESSING": return "处理中";
            case "RESOLVED": return "已解决";
            case "REJECTED": return "已拒绝";
            default: return status;
        }
    }
}
