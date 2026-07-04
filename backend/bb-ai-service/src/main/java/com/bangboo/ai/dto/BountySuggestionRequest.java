package com.bangboo.ai.dto;

/** 赏金建议请求。对齐 ai.ts bountySuggestion data: {category?, description?, rawText?}。 */
public record BountySuggestionRequest(
        String category,
        String description,
        String rawText
) {
}
