package com.bangboo.marketplace.dto;

import com.bangboo.marketplace.enums.ApplicationStatus;

import java.time.Instant;

/** 接单申请视图对象，字段对齐前端 types/application.ts TaskApplicationVO。 */
public record TaskApplicationVO(
        Long id,
        Long taskId,
        String taskTitle,
        Long hunterId,
        String hunterName,
        String hunterAvatar,
        Integer hunterLevel,
        String hunterTitle,
        Integer hunterReputation,
        String applyMessage,
        Instant expectedFinishTime,
        ApplicationStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
