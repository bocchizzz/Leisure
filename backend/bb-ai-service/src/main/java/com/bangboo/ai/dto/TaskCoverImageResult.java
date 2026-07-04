package com.bangboo.ai.dto;

/** 任务封面生成响应。对齐 ai.ts TaskCoverImageResult。 */
public record TaskCoverImageResult(
        String imageUrl,
        String prompt,
        String source
) {
}
