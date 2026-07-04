package com.bangboo.ai.dto;

/** 对话响应。对齐 ai.ts ChatResponse: {reply, action?, data?}。 */
public record ChatResponse(
        String reply,
        String action,
        Object data
) {
}
