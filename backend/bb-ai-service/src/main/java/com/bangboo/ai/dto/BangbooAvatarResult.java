package com.bangboo.ai.dto;

/**
 * 邦布头像生成响应。对齐 ai.ts BangbooAvatarResult。
 * imageUrl 需能被前端作为 bangboo-ai:<imageUrl> 用于 PUT /users/profile。
 */
public record BangbooAvatarResult(
        String imageUrl,
        String prompt,
        String source,
        String mascotKey
) {
}
