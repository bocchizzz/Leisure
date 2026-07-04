package com.bangboo.reputation.controller;

import com.bangboo.reputation.dto.internal.CreditAdjustmentRequest;
import com.bangboo.reputation.dto.internal.CreditAdjustmentResultVO;
import com.bangboo.reputation.dto.internal.ReputationStatsVO;
import com.bangboo.reputation.repository.CreditLogRepository;
import com.bangboo.reputation.repository.ReviewRepository;
import com.bangboo.reputation.service.CreditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * reputation 对内部服务提供的接口，仅允许携带 X-Internal-Token 的服务间调用。
 * - credit-adjustments：供 marketplace / court / 自身评价逻辑调整信誉。
 * - stats/reputation：供 admin-ops。
 */
@RestController
@RequestMapping("/internal")
public class InternalController {
    private final CreditService creditService;
    private final ReviewRepository reviewRepository;
    private final CreditLogRepository creditLogRepository;

    public InternalController(
            CreditService creditService,
            ReviewRepository reviewRepository,
            CreditLogRepository creditLogRepository
    ) {
        this.creditService = creditService;
        this.reviewRepository = reviewRepository;
        this.creditLogRepository = creditLogRepository;
    }

    /** 信誉变更：记 rep_credit_log，更新本地快照，同步 IAM，返回前后分值。 */
    @PostMapping("/credit-adjustments")
    public CreditAdjustmentResultVO adjust(@RequestBody CreditAdjustmentRequest request) {
        return creditService.adjust(request);
    }

    /** reputation 统计，供 admin-ops 看板。 */
    @GetMapping("/stats/reputation")
    public ReputationStatsVO stats() {
        return new ReputationStatsVO(reviewRepository.count(), creditLogRepository.count());
    }
}
