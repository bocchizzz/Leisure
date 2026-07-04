package com.bangboo.adminops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAuditLogRequest(
        @NotNull Long operatorId,
        String operatorName,
        @NotBlank String action,
        String targetType,
        Long targetId,
        String detail
) {
}
