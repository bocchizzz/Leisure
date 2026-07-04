package com.bangboo.ai.dto;

/** 任务封面生成请求。对齐 ai.ts TaskCoverImageRequest。 */
public record TaskCoverImageRequest(
        String title,
        String description,
        String category,
        String referenceImageUrl
) {
}
