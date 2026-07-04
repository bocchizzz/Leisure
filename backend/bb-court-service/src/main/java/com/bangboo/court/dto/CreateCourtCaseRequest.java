package com.bangboo.court.dto;

import com.bangboo.court.enums.CourtCaseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCourtCaseRequest(
        @NotNull Long contractId,
        @NotNull CourtCaseType type,
        @NotBlank @Size(max = 120) String caseTitle,
        @NotBlank @Size(max = 1000) String content,
        @Size(max = 200) String contact
) {
}
