package com.bangboo.court.dto;

import com.bangboo.court.enums.EvidenceType;

import java.time.Instant;

public record CourtEvidenceVO(
        Long id,
        Long caseId,
        Long submitterId,
        String submitterName,
        EvidenceType type,
        String fileUrl,
        String content,
        Instant createdAt
) {
}
