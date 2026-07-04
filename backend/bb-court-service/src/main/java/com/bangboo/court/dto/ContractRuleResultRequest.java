package com.bangboo.court.dto;

import com.bangboo.court.enums.RulingResult;

public record ContractRuleResultRequest(
        Long caseId,
        RulingResult result,
        double bountyReleaseRate,
        int publisherCreditDelta,
        int hunterCreditDelta,
        String reason
) {
}
