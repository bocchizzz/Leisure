package com.bangboo.ai.dto;

import java.util.List;

/** 案件摘要响应。对齐 ai.ts CourtSummaryResult。 */
public record CourtSummaryResult(
        String summary,
        List<String> focusPoints,
        String evidenceAnalysis,
        String suggestion
) {
}
