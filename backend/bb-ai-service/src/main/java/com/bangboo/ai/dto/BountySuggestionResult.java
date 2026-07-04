package com.bangboo.ai.dto;

/** 赏金建议响应。对齐 ai.ts BountySuggestionResult。 */
public record BountySuggestionResult(
        Integer suggestedBountyMin,
        Integer suggestedBountyMax,
        String reason
) {
}
