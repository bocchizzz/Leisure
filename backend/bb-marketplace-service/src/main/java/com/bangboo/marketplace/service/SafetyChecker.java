package com.bangboo.marketplace.service;

import com.bangboo.common.constant.ErrorCode;
import com.bangboo.common.exception.BusinessException;
import com.bangboo.marketplace.client.InternalApiClient;
import com.bangboo.marketplace.dto.internal.OpsConfigVO;
import com.bangboo.marketplace.enums.SafetyStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 第一版轻量内容安全：基于运营配置的禁用关键词做匹配。
 * 命中禁用词 -> 抛 422（内容安全拦截）；否则标记 PASS。
 */
@Service
public class SafetyChecker {
    private final InternalApiClient internalApiClient;

    public SafetyChecker(InternalApiClient internalApiClient) {
        this.internalApiClient = internalApiClient;
    }

    public record SafetyResult(SafetyStatus status, Double score, String reason, List<String> labels) {
    }

    /** 校验任务文本；命中禁用词直接抛出 422。 */
    public SafetyResult checkTaskContent(String title, String description) {
        OpsConfigVO config = internalApiClient.opsConfig();
        String text = ((title == null ? "" : title) + " " + (description == null ? "" : description))
                .toLowerCase(Locale.ROOT);

        List<String> hits = new ArrayList<>();
        List<String> banned = config.bannedKeywords();
        if (banned != null) {
            for (String keyword : banned) {
                if (keyword == null || keyword.isBlank()) {
                    continue;
                }
                if (text.contains(keyword.trim().toLowerCase(Locale.ROOT))) {
                    hits.add(keyword.trim());
                }
            }
        }

        if (!hits.isEmpty()) {
            throw new BusinessException(ErrorCode.UNPROCESSABLE_ENTITY, "内容包含违规信息：" + String.join("、", hits));
        }
        return new SafetyResult(SafetyStatus.PASS, 20.0, null, List.of());
    }
}
