package com.bangboo.reputation.service;

import com.bangboo.reputation.client.InternalApiClient;
import com.bangboo.reputation.dto.internal.CreditAdjustmentRequest;
import com.bangboo.reputation.dto.internal.CreditAdjustmentResultVO;
import com.bangboo.reputation.dto.internal.ReputationAdjustmentResultVO;
import com.bangboo.reputation.dto.internal.UserBriefVO;
import com.bangboo.reputation.entity.CreditLog;
import com.bangboo.reputation.entity.HunterStats;
import com.bangboo.reputation.repository.CreditLogRepository;
import com.bangboo.reputation.repository.HunterStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * 信誉变更核心。
 * 记 rep_credit_log，更新本地 HunterStats 信誉快照，并同步 IAM 当前信誉分。
 * 被 POST /internal/credit-adjustments 与评价逻辑复用。
 */
@Service
public class CreditService {
    /** 用户首次出现时的默认基准信誉分（与种子/展示一致）。 */
    static final int DEFAULT_REPUTATION = 500;

    private final CreditLogRepository creditLogRepository;
    private final HunterStatsRepository hunterStatsRepository;
    private final InternalApiClient internalApiClient;
    private final IdGenerator idGenerator;

    public CreditService(
            CreditLogRepository creditLogRepository,
            HunterStatsRepository hunterStatsRepository,
            InternalApiClient internalApiClient,
            IdGenerator idGenerator
    ) {
        this.creditLogRepository = creditLogRepository;
        this.hunterStatsRepository = hunterStatsRepository;
        this.internalApiClient = internalApiClient;
        this.idGenerator = idGenerator;
    }

    /**
     * 调整用户信誉分：
     * 1) 记录 rep_credit_log（before/after）。
     * 2) 更新本地 HunterStats.reputation。
     * 3) 同步 IAM 当前信誉分（best-effort）。
     */
    @Transactional
    public CreditAdjustmentResultVO adjust(CreditAdjustmentRequest request) {
        Long userId = request.userId();
        int delta = request.delta() == null ? 0 : request.delta();

        HunterStats stats = getOrCreateStats(userId);
        int before = stats.getReputation() == null ? DEFAULT_REPUTATION : stats.getReputation();
        int after = before + delta;

        CreditLog log = new CreditLog();
        log.setId(idGenerator.nextCreditLogId());
        log.setUserId(userId);
        log.setDelta(delta);
        log.setBeforeScore(before);
        log.setAfterScore(after);
        log.setSourceType(request.sourceType());
        log.setSourceId(request.sourceId());
        log.setReason(request.reason());
        log.setCreatedAt(Instant.now());
        creditLogRepository.save(log);

        stats.setReputation(after);
        stats.setUpdatedAt(Instant.now());
        hunterStatsRepository.save(stats);

        // 同步 IAM 当前信誉分（best-effort，失败仅记日志，不回滚本地流水）
        internalApiClient.adjustIamReputation(userId, delta, request.sourceType(), request.sourceId(), request.reason());

        return new CreditAdjustmentResultVO(userId, before, after);
    }

    /** 获取或初始化用户统计快照；初始信誉尝试从 IAM brief 读取，否则用默认值。 */
    @Transactional
    public HunterStats getOrCreateStats(Long userId) {
        return hunterStatsRepository.findById(userId).orElseGet(() -> {
            HunterStats stats = new HunterStats();
            stats.setUserId(userId);
            Optional<UserBriefVO> brief = internalApiClient.userBrief(userId);
            stats.setNickname(brief.map(UserBriefVO::nickname).orElse("用户" + userId));
            stats.setAvatarUrl(brief.map(UserBriefVO::avatarUrl).orElse("bangboo:kacha"));
            int reputation = brief.map(UserBriefVO::reputation).filter(r -> r != null).orElse(DEFAULT_REPUTATION);
            stats.setReputation(reputation);
            int level = brief.map(UserBriefVO::hunterLevel).filter(l -> l != null).orElse(0);
            stats.setLevel(level);
            stats.setTitle(brief.map(UserBriefVO::hunterTitle).filter(t -> t != null && !t.isBlank())
                    .orElse(HunterLevels.titleForLevel(level)));
            stats.setXp(0);
            stats.setCompletedTaskCount(0);
            stats.setPositiveReviewCount(0);
            stats.setTotalReviewCount(0);
            stats.setOnTimeCount(0);
            stats.setArbitrationAcceptedCount(0);
            stats.setBadges("");
            stats.setUpdatedAt(Instant.now());
            return hunterStatsRepository.save(stats);
        });
    }

    public long reviewIndependentCreditLogCount() {
        return creditLogRepository.count();
    }
}
