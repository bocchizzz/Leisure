package com.bangboo.court.dto;

import com.bangboo.court.enums.CourtVoteOption;

import java.time.Instant;

public record CourtVoteVO(
        Long id,
        Long caseId,
        Long voterId,
        String voterName,
        CourtVoteOption option,
        int weight,
        String reason,
        boolean isAdopted,
        Instant createdAt
) {
}
