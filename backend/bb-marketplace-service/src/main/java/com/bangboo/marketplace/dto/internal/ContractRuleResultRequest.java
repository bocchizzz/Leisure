package com.bangboo.marketplace.dto.internal;

/** 契约裁决结果请求，court 调用 rule-result 时传入。 */
public record ContractRuleResultRequest(
        Long caseId,
        String result,
        Double bountyReleaseRate,
        String reason
) {
}
