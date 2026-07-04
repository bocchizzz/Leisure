package com.bangboo.ai.dto;

/** 邦布头像生成请求。对齐 ai.ts BangbooAvatarRequest。 */
public record BangbooAvatarRequest(
        String referenceImageUrl,
        String mascotKey,
        String seed
) {
}
