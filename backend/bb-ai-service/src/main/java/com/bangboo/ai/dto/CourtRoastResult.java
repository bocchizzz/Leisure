package com.bangboo.ai.dto;

/** 案件点评响应。对齐 ai.ts CourtRoastResult。 */
public record CourtRoastResult(
        String roast,
        String style
) {
}
