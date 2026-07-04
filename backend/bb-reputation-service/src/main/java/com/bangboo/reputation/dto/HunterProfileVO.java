package com.bangboo.reputation.dto;

import java.util.List;

/** 猎人档案视图对象。字段对齐前端 user.ts HunterProfileVO。 */
public record HunterProfileVO(
        Long userId,
        String nickname,
        String avatarUrl,
        Integer level,
        String title,
        Integer xp,
        Integer nextLevelXp,
        Integer reputation,
        Integer completedTaskCount,
        Double onTimeRate,
        Double positiveRate,
        Integer arbitrationAcceptedCount,
        List<String> badges
) {
}
