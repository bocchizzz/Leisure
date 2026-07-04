package com.bangboo.court.dto;

import com.bangboo.court.enums.RulingResult;

import java.time.Instant;

public record CourtRulingVO(
        Long id,
        Long caseId,
        Long adminId,
        String adminName,
        RulingResult result,
        double bountyReleaseRate,
        int publisherCreditDelta,
        int hunterCreditDelta,
        String reason,
        Instant createdAt
) {
}
