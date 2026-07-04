package com.bangboo.reputation.service;

import com.bangboo.reputation.dto.CreditLogVO;
import com.bangboo.reputation.dto.HunterProfileVO;
import com.bangboo.reputation.dto.LeaderboardEntryVO;
import com.bangboo.reputation.dto.ReviewVO;
import com.bangboo.reputation.entity.CreditLog;
import com.bangboo.reputation.entity.HunterStats;
import com.bangboo.reputation.entity.Review;

import java.util.Arrays;
import java.util.List;

/** 实体 -> VO 映射；逗号分隔字段转数组；比率计算。 */
public final class ReputationMapper {
    private ReputationMapper() {
    }

    public static ReviewVO toReviewVO(Review r) {
        return new ReviewVO(
                r.getId(),
                r.getContractId(),
                r.getTaskId(),
                r.getTaskTitle(),
                r.getReviewerId(),
                r.getReviewerName(),
                r.getReviewerAvatar(),
                r.getRevieweeId(),
                r.getRevieweeName(),
                r.getRole(),
                r.getRating(),
                splitCsv(r.getTags()),
                r.getContent(),
                r.getCreatedAt()
        );
    }

    public static CreditLogVO toCreditLogVO(CreditLog c) {
        return new CreditLogVO(
                c.getId(),
                c.getUserId(),
                c.getDelta(),
                c.getBeforeScore(),
                c.getAfterScore(),
                c.getSourceType(),
                c.getSourceId(),
                c.getReason(),
                c.getCreatedAt()
        );
    }

    public static HunterProfileVO toHunterProfileVO(HunterStats s) {
        int level = s.getLevel() == null ? 0 : s.getLevel();
        return new HunterProfileVO(
                s.getUserId(),
                s.getNickname(),
                s.getAvatarUrl(),
                level,
                s.getTitle(),
                nz(s.getXp()),
                HunterLevels.nextLevelXp(level),
                nz(s.getReputation()),
                nz(s.getCompletedTaskCount()),
                rate(s.getOnTimeCount(), s.getCompletedTaskCount()),
                rate(s.getPositiveReviewCount(), s.getTotalReviewCount()),
                nz(s.getArbitrationAcceptedCount()),
                splitCsv(s.getBadges())
        );
    }

    public static LeaderboardEntryVO toLeaderboardEntry(HunterStats s, int rank) {
        return new LeaderboardEntryVO(
                rank,
                s.getUserId(),
                s.getNickname(),
                s.getAvatarUrl(),
                s.getLevel() == null ? 0 : s.getLevel(),
                s.getTitle(),
                nz(s.getXp()),
                nz(s.getCompletedTaskCount()),
                nz(s.getReputation())
        );
    }

    public static List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /** 比率：分子/分母，保留两位小数；分母为0返回 null。 */
    private static Double rate(Integer numerator, Integer denominator) {
        int d = nz(denominator);
        if (d <= 0) {
            return null;
        }
        int n = nz(numerator);
        return Math.round((double) n / d * 100.0) / 100.0;
    }

    private static int nz(Integer value) {
        return value == null ? 0 : value;
    }
}
