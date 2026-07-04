package com.bangboo.ai.dto;

/** 对话请求。对齐 ai.ts chat data: {message, context?}。 */
public record ChatRequest(
        String message,
        String context
) {
}
