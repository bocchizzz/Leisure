package com.bangboo.marketplace.controller;

import com.bangboo.marketplace.dto.internal.ContractBriefVO;
import com.bangboo.marketplace.dto.internal.ContractRuleResultRequest;
import com.bangboo.marketplace.dto.internal.MarkDisputedRequest;
import com.bangboo.marketplace.dto.internal.MarketplaceStatsVO;
import com.bangboo.marketplace.dto.internal.ReviewFlagRequest;
import com.bangboo.marketplace.dto.internal.ReviewPermissionVO;
import com.bangboo.marketplace.dto.TaskContractVO;
import com.bangboo.marketplace.service.ContractService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * marketplace 对内部服务提供的接口，仅允许携带 X-Internal-Token 的服务间调用。
 * - contracts/{id}/brief：供 court / reputation
 * - contracts/{id}/mark-disputed、rule-result：供 court
 * - contracts/{id}/review-permission、review-flags：供 reputation
 * - stats/marketplace：供 admin-ops
 */
@RestController
@RequestMapping("/internal")
public class InternalController {
    private final ContractService contractService;

    public InternalController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping("/contracts/{id}/brief")
    public ContractBriefVO contractBrief(@PathVariable Long id) {
        return contractService.brief(id);
    }

    @PostMapping("/contracts/{id}/mark-disputed")
    public ContractBriefVO markDisputed(@PathVariable Long id, @RequestBody(required = false) MarkDisputedRequest request) {
        return contractService.markDisputed(id, request);
    }

    @PostMapping("/contracts/{id}/rule-result")
    public ContractBriefVO ruleResult(@PathVariable Long id, @RequestBody(required = false) ContractRuleResultRequest request) {
        return contractService.applyRuleResult(id, request);
    }

    @GetMapping("/contracts/{id}/review-permission")
    public ReviewPermissionVO reviewPermission(@PathVariable Long id, @RequestParam Long userId) {
        return contractService.reviewPermission(id, userId);
    }

    @PostMapping("/contracts/{id}/review-flags")
    public TaskContractVO reviewFlags(@PathVariable Long id, @RequestBody ReviewFlagRequest request) {
        return contractService.markReviewFlag(id, request);
    }

    @GetMapping("/stats/marketplace")
    public MarketplaceStatsVO stats() {
        return contractService.stats();
    }
}
