package com.bangboo.ai.dto;

/** 风险评估请求。对齐 ai.ts riskAssessment data: {description?, rawText?}。 */
public record RiskAssessmentRequest(
        String description,
        String rawText
) {
}
