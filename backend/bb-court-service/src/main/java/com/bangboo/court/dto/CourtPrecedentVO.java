package com.bangboo.court.dto;

import com.bangboo.court.enums.RulingResult;

import java.time.Instant;
import java.util.List;

public record CourtPrecedentVO(
        Long id,
        Long caseId,
        String title,
        String summary,
        RulingResult rulingResult,
        List<String> tags,
        Instant createdAt
) {
}
