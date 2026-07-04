package com.bangboo.ai.dto;

/** 任务拆解请求。对齐 ai.ts breakdown data: {rawText}。 */
public record BreakdownRequest(
        String rawText
) {
}
