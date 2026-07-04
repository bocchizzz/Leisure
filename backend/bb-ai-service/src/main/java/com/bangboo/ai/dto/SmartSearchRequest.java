package com.bangboo.ai.dto;

/** 智能搜索请求。对齐 ai.ts smartSearch data: {rawText}。 */
public record SmartSearchRequest(
        String rawText
) {
}
