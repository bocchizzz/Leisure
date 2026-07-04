package com.bangboo.court.dto;

public record CreateMessageRequest(
        Long userId,
        String type,
        String title,
        String content,
        Long relatedId
) {
}
