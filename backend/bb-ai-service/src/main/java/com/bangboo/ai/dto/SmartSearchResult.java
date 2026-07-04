package com.bangboo.ai.dto;

import java.util.List;

/** 智能搜索响应。对齐 ai.ts smartSearch 返回: {keyword?, category?, tips?[]}。 */
public record SmartSearchResult(
        String keyword,
        String category,
        List<String> tips
) {
}
