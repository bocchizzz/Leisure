package com.bangboo.court.dto;

import com.bangboo.court.enums.CourtCaseStatus;
import com.bangboo.court.enums.CourtCaseType;

import java.time.Instant;

public record CourtCaseVO(
        Long id,
        String caseNo,
        Long taskId,
        String taskTitle,
        Long contractId,
        String caseTitle,
        CourtCaseType type,
        CourtCaseStatus status,
        Long plaintiffId,
        String plaintiffName,
        Long defendantId,
        String defendantName,
        String initialStatement,
        String summary,
        String aiSummary,
        String aiEvidenceAnalysis,
        String aiRoast,
        VoteStats voteStats,
        boolean myVoted,
        Instant createdAt,
        Instant ruledAt
) {
}
