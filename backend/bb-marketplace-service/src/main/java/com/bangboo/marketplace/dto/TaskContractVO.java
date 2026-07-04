package com.bangboo.marketplace.dto;

import com.bangboo.marketplace.enums.ContractStatus;

import java.time.Instant;

/** 任务契约视图对象，字段对齐前端 types/contract.ts TaskContractVO。 */
public record TaskContractVO(
        Long id,
        String contractNo,
        Long taskId,
        String taskTitle,
        Long applicationId,
        Long publisherId,
        String publisherName,
        String publisherAvatar,
        Long hunterId,
        String hunterName,
        String hunterAvatar,
        Double bountyAmount,
        ContractStatus status,
        String completionStandard,
        String evidenceRequirement,
        Boolean reviewedByPublisher,
        Boolean reviewedByHunter,
        String cancelReason,
        Instant acceptedAt,
        Instant submittedAt,
        Instant completedAt,
        Instant createdAt
) {
}
