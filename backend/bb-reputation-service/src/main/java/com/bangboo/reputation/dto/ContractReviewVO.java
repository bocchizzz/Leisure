package com.bangboo.reputation.dto;

/** 契约双方评价汇总。字段对齐前端 review.ts ContractReviewVO。 */
public record ContractReviewVO(
        ReviewVO publisherToHunter,
        ReviewVO hunterToPublisher
) {
}
