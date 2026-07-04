package com.bangboo.reputation.service;

/**
 * 猎人等级工具。称号对齐前端 enums.ts HunterTitles（Lv0~Lv6）。
 * 等级由累计经验值（xp）决定，每完成一单/获得好评累加 xp。
 */
public final class HunterLevels {
    /** 各等级所需的最低累计经验（下标即等级）。 */
    private static final int[] LEVEL_THRESHOLDS = {0, 50, 150, 350, 700, 1200, 2000};

    private static final String[] TITLES = {
            "见习猎人", "铁牌猎人", "铜牌猎人", "银牌猎人", "金牌猎人", "星徽猎人", "传奇猎人"
    };

    private HunterLevels() {
    }

    public static int levelForXp(int xp) {
        int level = 0;
        for (int i = 0; i < LEVEL_THRESHOLDS.length; i++) {
            if (xp >= LEVEL_THRESHOLDS[i]) {
                level = i;
            }
        }
        return level;
    }

    public static String titleForLevel(int level) {
        if (level < 0) {
            level = 0;
        }
        if (level >= TITLES.length) {
            level = TITLES.length - 1;
        }
        return TITLES[level];
    }

    /** 下一级所需累计经验；已达最高级返回 null。 */
    public static Integer nextLevelXp(int level) {
        int next = level + 1;
        if (next >= LEVEL_THRESHOLDS.length) {
            return null;
        }
        return LEVEL_THRESHOLDS[next];
    }
}
