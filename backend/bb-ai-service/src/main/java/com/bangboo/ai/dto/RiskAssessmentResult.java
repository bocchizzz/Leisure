package com.bangboo.ai.dto;

import java.util.List;

/** 风险评估响应。对齐 ai.ts RiskAssessmentResult。 */
public record RiskAssessmentResult(
        List<String> risks,
        List<String> suggestions
) {
}
