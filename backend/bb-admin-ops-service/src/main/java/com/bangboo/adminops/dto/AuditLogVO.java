package com.bangboo.adminops.dto;

import java.time.Instant;

public record AuditLogVO(
        Long id,
        Long operatorId,
        String operatorName,
        String action,
        String targetType,
        Long targetId,
        String detail,
        Instant createdAt
) {
}
