package com.bangboo.court.dto;

import com.bangboo.court.enums.CourtPartyRole;

import java.time.Instant;

public record CourtStatementVO(
        Long id,
        Long caseId,
        Long userId,
        String userName,
        String userAvatar,
        CourtPartyRole role,
        String content,
        Instant createdAt
) {
}
