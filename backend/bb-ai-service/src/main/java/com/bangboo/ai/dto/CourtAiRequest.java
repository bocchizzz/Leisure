package com.bangboo.ai.dto;

/** 案件摘要/点评请求。对齐 ai.ts courtSummary/courtRoast data: {caseId, style}。 */
public record CourtAiRequest(
        Long caseId,
        String style
) {
}
