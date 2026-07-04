package com.bangboo.marketplace.dto;

import com.bangboo.marketplace.enums.EvidenceType;

import java.time.Instant;

/** 履约证据视图对象，字段对齐前端 types/contract.ts TaskEvidenceVO。 */
public record TaskEvidenceVO(
        Long id,
        Long contractId,
        Long taskId,
        Long submitterId,
        String submitterName,
        EvidenceType type,
        String fileUrl,
        String content,
        Instant createdAt
) {
}
