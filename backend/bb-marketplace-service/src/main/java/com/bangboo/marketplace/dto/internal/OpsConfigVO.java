package com.bangboo.marketplace.dto.internal;

import java.util.List;

/**
 * 运营配置，仅取任务审核与内容安全相关字段。对齐 A 侧 admin-ops /internal/ops-config。
 * 未列出的字段在反序列化时忽略。
 */
public record OpsConfigVO(
        String taskReviewMode,
        Integer minAutoPassSafetyScore,
        Integer maxAutoBlockSafetyScore,
        Boolean aiSafetyEnabled,
        List<String> bannedKeywords
) {
    public static OpsConfigVO defaults() {
        return new OpsConfigVO("MANUAL", 30, 80, true, List.of());
    }
}
