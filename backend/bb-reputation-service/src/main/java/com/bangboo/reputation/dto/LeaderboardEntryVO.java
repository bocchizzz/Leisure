package com.bangboo.reputation.dto;

/** 榜单条目。字段对齐前端 user.ts LeaderboardEntryVO。 */
public record LeaderboardEntryVO(
        Integer rank,
        Long userId,
        String nickname,
        String avatarUrl,
        Integer level,
        String title,
        Integer xp,
        Integer completedTaskCount,
        Integer reputation
) {
}
