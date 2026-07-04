package com.bangboo.message.dto;

import com.bangboo.message.enums.MessageType;

import java.time.Instant;

public record MessageVO(
        Long id,
        Long userId,
        MessageType type,
        String title,
        String content,
        boolean isRead,
        Long relatedId,
        Instant createdAt
) {
}
