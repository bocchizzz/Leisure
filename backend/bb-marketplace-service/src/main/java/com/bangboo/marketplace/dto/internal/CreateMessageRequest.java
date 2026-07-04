package com.bangboo.marketplace.dto.internal;

/** 创建站内消息请求，对齐 A 侧 message /internal/messages。 */
public record CreateMessageRequest(
        Long userId,
        String type,
        String title,
        String content,
        Long relatedId
) {
}
