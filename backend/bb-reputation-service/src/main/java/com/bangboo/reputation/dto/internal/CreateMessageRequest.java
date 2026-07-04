package com.bangboo.reputation.dto.internal;

/** 创建站内信请求。对齐 A 侧 /internal/messages。 */
public record CreateMessageRequest(
        Long userId,
        String type,
        String title,
        String content,
        Long relatedId
) {
}
