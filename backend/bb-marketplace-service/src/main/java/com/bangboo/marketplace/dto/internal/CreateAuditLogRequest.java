package com.bangboo.marketplace.dto.internal;

/** 创建审计日志请求，对齐 A 侧 admin-ops /internal/audit-logs。 */
public record CreateAuditLogRequest(
        Long operatorId,
        String action,
        String targetType,
        Long targetId,
        String detail
) {
}
