package com.bangboo.reputation.service;

import com.bangboo.common.exception.BusinessException;
import com.bangboo.common.exception.ForbiddenException;
import com.bangboo.common.security.CurrentUser;
import com.bangboo.reputation.client.InternalApiClient;
import com.bangboo.reputation.dto.ContractReviewVO;
import com.bangboo.reputation.dto.CreateReviewRequest;
import com.bangboo.reputation.dto.ReviewVO;
import com.bangboo.reputation.dto.internal.CreateMessageRequest;
import com.bangboo.reputation.dto.internal.CreditAdjustmentRequest;
import com.bangboo.reputation.dto.internal.ReviewFlagRequest;
import com.bangboo.reputation.dto.internal.ReviewPermissionVO;
import com.bangboo.reputation.dto.internal.UserBriefVO;
import com.bangboo.reputation.entity.HunterStats;
import com.bangboo.reputation.entity.Review;
import com.bangboo.reputation.enums.ReviewRole;
import com.bangboo.reputation.repository.HunterStatsRepository;
import com.bangboo.reputation.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final HunterStatsRepository hunterStatsRepository;
    private final ReputationAuthService authService;
    private final InternalApiClient internalApiClient;
    private final CreditService creditService;
    private final IdGenerator idGenerator;

    public ReviewService(
            ReviewRepository reviewRepository,
            HunterStatsRepository hunterStatsRepository,
            ReputationAuthService authService,
            InternalApiClient internalApiClient,
            CreditService creditService,
            IdGenerator idGenerator
    ) {
        this.reviewRepository = reviewRepository;
        this.hunterStatsRepository = hunterStatsRepository;
        this.authService = authService;
        this.internalApiClient = internalApiClient;
        this.creditService = creditService;
        this.idGenerator = idGenerator;
    }

    /**
     * 提交评价：
     * - 校验契约评价权限（调 marketplace，必须成功）。
     * - 契约未完成 -> 409；非双方 -> 403；重复评价 -> 409。
     * - 保存评价并更新被评价人统计。
     * - 回调 marketplace 标记评价状态；写信誉流水并同步 IAM；通知被评价人。
     */
    @Transactional
    public ReviewVO create(CreateReviewRequest request) {
        CurrentUser user = authService.requireUser();
        Long reviewerId = user.uid();

        ReviewPermissionVO perm = internalApiClient.reviewPermission(request.contractId(), reviewerId);

        boolean isParty = reviewerId.equals(perm.publisherId()) || reviewerId.equals(perm.hunterId());
        if (!isParty) {
            throw new ForbiddenException("只有契约双方可以评价");
        }
        if (!"COMPLETED".equals(perm.status())) {
            throw new BusinessException(409, "契约完成后才能评价");
        }

        ReviewRole role = reviewerId.equals(perm.publisherId())
                ? ReviewRole.PUBLISHER_TO_HUNTER
                : ReviewRole.HUNTER_TO_PUBLISHER;
        Long revieweeId = role == ReviewRole.PUBLISHER_TO_HUNTER ? perm.hunterId() : perm.publisherId();

        if (reviewRepository.existsByContractIdAndRole(request.contractId(), role)) {
            throw new BusinessException(409, "你已经评价过了");
        }

        Optional<UserBriefVO> reviewerBrief = internalApiClient.userBrief(reviewerId);
        Optional<UserBriefVO> revieweeBrief = internalApiClient.userBrief(revieweeId);

        Review review = new Review();
        review.setId(idGenerator.nextReviewId());
        review.setContractId(request.contractId());
        review.setTaskId(perm.taskId());
        review.setTaskTitle(perm.taskTitle());
        review.setReviewerId(reviewerId);
        review.setReviewerName(reviewerBrief.map(UserBriefVO::nickname).orElse("用户" + reviewerId));
        review.setReviewerAvatar(reviewerBrief.map(UserBriefVO::avatarUrl).orElse("bangboo:kacha"));
        review.setRevieweeId(revieweeId);
        review.setRevieweeName(revieweeBrief.map(UserBriefVO::nickname).orElse("用户" + revieweeId));
        review.setRole(role);
        review.setRating(request.rating());
        review.setTags(joinCsv(request.tags()));
        review.setContent(request.content());
        review.setCreatedAt(Instant.now());
        Review saved = reviewRepository.save(review);

        // 更新被评价人统计（评价数、好评数、经验）
        updateRevieweeStats(revieweeId, request.rating(), revieweeBrief);

        // 回调 marketplace 标记契约评价状态（best-effort）
        internalApiClient.markReviewFlag(request.contractId(),
                new ReviewFlagRequest(reviewerId, role.name(), saved.getId()));

        // 写信誉流水 + 同步 IAM（评分转信誉增量）
        int delta = creditDeltaForRating(request.rating());
        creditService.adjust(new CreditAdjustmentRequest(
                revieweeId, delta, "REVIEW", request.contractId(),
                "收到评价（" + request.rating() + "星）"
        ));

        // 通知被评价人
        internalApiClient.createMessage(new CreateMessageRequest(
                revieweeId,
                "REVIEW",
                "你收到一条新评价",
                "《" + safe(perm.taskTitle()) + "》的对方给了你 " + request.rating() + " 星评价",
                request.contractId()
        ));

        return ReputationMapper.toReviewVO(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewVO> byUser(Long userId) {
        return reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId).stream()
                .map(ReputationMapper::toReviewVO)
                .toList();
    }

    /** 任务评价：需相关方或管理员。 */
    @Transactional(readOnly = true)
    public List<ReviewVO> byTask(Long taskId) {
        authService.requireUser();
        return reviewRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(ReputationMapper::toReviewVO)
                .toList();
    }

    /** 契约双方评价汇总：需相关方或管理员。 */
    @Transactional(readOnly = true)
    public ContractReviewVO byContract(Long contractId) {
        CurrentUser user = authService.requireUser();
        List<Review> list = reviewRepository.findByContractId(contractId);
        boolean isParty = list.stream().anyMatch(r ->
                user.uid().equals(r.getReviewerId()) || user.uid().equals(r.getRevieweeId()));
        if (!list.isEmpty() && !isParty && !authService.isAdmin(user)) {
            throw new ForbiddenException("非契约相关方不能查看评价");
        }
        ReviewVO publisherToHunter = list.stream()
                .filter(r -> r.getRole() == ReviewRole.PUBLISHER_TO_HUNTER)
                .findFirst().map(ReputationMapper::toReviewVO).orElse(null);
        ReviewVO hunterToPublisher = list.stream()
                .filter(r -> r.getRole() == ReviewRole.HUNTER_TO_PUBLISHER)
                .findFirst().map(ReputationMapper::toReviewVO).orElse(null);
        return new ContractReviewVO(publisherToHunter, hunterToPublisher);
    }

    // ---------- helpers ----------

    private void updateRevieweeStats(Long revieweeId, int rating, Optional<UserBriefVO> brief) {
        HunterStats stats = creditService.getOrCreateStats(revieweeId);
        // 用最新 brief 补齐昵称头像快照
        brief.ifPresent(b -> {
            if (b.nickname() != null) {
                stats.setNickname(b.nickname());
            }
            if (b.avatarUrl() != null) {
                stats.setAvatarUrl(b.avatarUrl());
            }
        });
        stats.setTotalReviewCount(nz(stats.getTotalReviewCount()) + 1);
        if (rating >= 4) {
            stats.setPositiveReviewCount(nz(stats.getPositiveReviewCount()) + 1);
        }
        int xp = nz(stats.getXp()) + xpForRating(rating);
        stats.setXp(xp);
        int level = HunterLevels.levelForXp(xp);
        stats.setLevel(level);
        stats.setTitle(HunterLevels.titleForLevel(level));
        stats.setUpdatedAt(Instant.now());
        hunterStatsRepository.save(stats);
    }

    /** 评分转信誉增量：5→+10, 4→+6, 3→+2, 2→-4, 1→-8。 */
    private int creditDeltaForRating(int rating) {
        return switch (rating) {
            case 5 -> 10;
            case 4 -> 6;
            case 3 -> 2;
            case 2 -> -4;
            default -> -8;
        };
    }

    /** 评分转经验：高分给更多经验，低分不减经验（下限0）。 */
    private int xpForRating(int rating) {
        return switch (rating) {
            case 5 -> 20;
            case 4 -> 15;
            case 3 -> 8;
            default -> 3;
        };
    }

    private static String joinCsv(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return String.join(",", tags.stream().filter(t -> t != null && !t.isBlank()).toList());
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
