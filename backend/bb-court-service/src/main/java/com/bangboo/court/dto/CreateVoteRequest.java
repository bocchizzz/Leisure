package com.bangboo.court.dto;

import com.bangboo.court.enums.CourtVoteOption;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateVoteRequest(
        @NotNull CourtVoteOption option,
        @Size(max = 300) String reason
) {
}
