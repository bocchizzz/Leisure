package com.bangboo.ai.dto;

import java.util.List;

/** 任务拆解响应。对齐 ai.ts TaskBreakdownResult。 */
public record TaskBreakdownResult(
        String title,
        String category,
        String difficulty,
        Integer suggestedBountyMin,
        Integer suggestedBountyMax,
        List<String> steps,
        List<String> riskTips
) {
}
