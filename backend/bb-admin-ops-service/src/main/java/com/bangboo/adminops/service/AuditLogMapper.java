package com.bangboo.adminops.service;

import com.bangboo.adminops.dto.AuditLogVO;
import com.bangboo.adminops.entity.AuditLog;

public final class AuditLogMapper {
    private AuditLogMapper() {
    }

    public static AuditLogVO toVO(AuditLog log) {
        return new AuditLogVO(
                log.getId(),
                log.getOperatorId(),
                log.getOperatorName(),
                log.getAction(),
                log.getTargetType(),
                log.getTargetId(),
                log.getDetail(),
                log.getCreatedAt()
        );
    }
}
