package com.bangboo.reputation.service;

import com.bangboo.reputation.repository.CreditLogRepository;
import com.bangboo.reputation.repository.ReviewRepository;
import org.springframework.stereotype.Component;

/**
 * 应用层主键分配（与 marketplace / court 风格一致）。
 * review 起始基线避开种子固定 ID 段（种子评价从 1000 起，业务从 1000+ 起）。
 */
@Component
public class IdGenerator {
    private static final long REVIEW_BASE = 1000L;
    private static final long CREDIT_LOG_BASE = 100L;

    private final ReviewRepository reviewRepository;
    private final CreditLogRepository creditLogRepository;

    public IdGenerator(ReviewRepository reviewRepository, CreditLogRepository creditLogRepository) {
        this.reviewRepository = reviewRepository;
        this.creditLogRepository = creditLogRepository;
    }

    public synchronized Long nextReviewId() {
        return Math.max(reviewRepository.maxId(), REVIEW_BASE) + 1;
    }

    public synchronized Long nextCreditLogId() {
        return Math.max(creditLogRepository.maxId(), CREDIT_LOG_BASE) + 1;
    }
}
