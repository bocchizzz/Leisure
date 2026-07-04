package com.bangboo.message.dto;

import com.bangboo.message.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMessageRequest(
        @NotNull Long userId,
        @NotNull MessageType type,
        @NotBlank String title,
        @NotBlank String content,
        Long relatedId
) {
}
