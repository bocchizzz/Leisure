package com.bangboo.court.dto;

public record CreateAuditLogRequest(
        Long operatorId,
        String operatorName,
        String action,
        String targetType,
        Long targetId,
        String detail
) {
}
