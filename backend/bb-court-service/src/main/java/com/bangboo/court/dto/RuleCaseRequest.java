package com.bangboo.court.dto;

import com.bangboo.court.enums.RulingResult;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RuleCaseRequest(
        @NotNull RulingResult result,
        @DecimalMin("0.0") @DecimalMax("1.0") double bountyReleaseRate,
        int publisherCreditDelta,
        int hunterCreditDelta,
        @NotBlank @Size(max = 1000) String reason,
        Boolean archiveAsPrecedent
) {
    public boolean shouldArchiveAsPrecedent() {
        return Boolean.TRUE.equals(archiveAsPrecedent);
    }
}
