package com.bangboo.court.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStatementRequest(
        @NotBlank @Size(max = 1000) String content
) {
}
