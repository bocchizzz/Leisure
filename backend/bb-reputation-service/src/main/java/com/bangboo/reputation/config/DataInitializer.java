package com.bangboo.reputation.config;

import com.bangboo.reputation.entity.CreditLog;
import com.bangboo.reputation.entity.HunterStats;
import com.bangboo.reputation.repository.CreditLogRepository;
import com.bangboo.reputation.repository.HunterStatsRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

/**
 * reputation 种子数据。
 * 复刻 mock-b-services 的猎人统计与信誉日志基准（hunter01=3 金牌、hunter02=4 银牌、jury01=5），
 * 保证前端猎人主页/榜单/信誉日志页有可展示数据，且不与业务新增数据的 ID 段冲突。
 */
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner reputationSeedRunner(
            HunterStatsRepository hunterStatsRepository,
            CreditLogRepository creditLogRepository
    ) {
        return args -> {
            if (hunterStatsRepository.count() == 0) {
                hunterStatsRepository.save(stats(3L, "热心猎人", "bangboo:kacha",
                        4, "金牌猎人", 760, 920, 12, 11, 12, 11, 2, "ON_TIME,GOOD_COMMUNICATION,COURT_JUROR"));
                hunterStatsRepository.save(stats(4L, "银牌猎人", "bangboo:elf",
                        3, "银牌猎人", 420, 875, 5, 4, 5, 4, 0, "ON_TIME,GOOD_COMMUNICATION"));
                hunterStatsRepository.save(stats(5L, "陪审猎人", "bangboo:fan",
                        3, "银牌猎人", 500, 890, 6, 5, 6, 5, 3, "COURT_JUROR,GOOD_COMMUNICATION"));
            }

            if (creditLogRepository.count() == 0) {
                creditLogRepository.save(creditLog(1L, 3L, 8, 912, 920,
                        "CONTRACT_COMPLETE", 302L, "完成任务"));
                creditLogRepository.save(creditLog(2L, 3L, -1, 913, 912,
                        "COURT_RULING", 401L, "小法庭裁决调整"));
                creditLogRepository.save(creditLog(3L, 4L, 6, 869, 875,
                        "REVIEW", 302L, "收到评价（4星）"));
            }
        };
    }

    private HunterStats stats(Long userId, String nickname, String avatar, int level, String title,
                              int xp, int reputation, int completed, int positive, int totalReview,
                              int onTime, int arbitration, String badges) {
        HunterStats s = new HunterStats();
        s.setUserId(userId);
        s.setNickname(nickname);
        s.setAvatarUrl(avatar);
        s.setLevel(level);
        s.setTitle(title);
        s.setXp(xp);
        s.setReputation(reputation);
        s.setCompletedTaskCount(completed);
        s.setPositiveReviewCount(positive);
        s.setTotalReviewCount(totalReview);
        s.setOnTimeCount(onTime);
        s.setArbitrationAcceptedCount(arbitration);
        s.setBadges(badges);
        s.setUpdatedAt(Instant.now());
        return s;
    }

    private CreditLog creditLog(Long id, Long userId, int delta, int before, int after,
                                String sourceType, Long sourceId, String reason) {
        CreditLog c = new CreditLog();
        c.setId(id);
        c.setUserId(userId);
        c.setDelta(delta);
        c.setBeforeScore(before);
        c.setAfterScore(after);
        c.setSourceType(sourceType);
        c.setSourceId(sourceId);
        c.setReason(reason);
        c.setCreatedAt(Instant.now());
        return c;
    }
}
