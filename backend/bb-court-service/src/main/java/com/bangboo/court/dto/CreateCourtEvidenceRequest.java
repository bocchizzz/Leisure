package com.bangboo.court.dto;

import com.bangboo.court.enums.EvidenceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCourtEvidenceRequest(
        @NotNull EvidenceType type,
        @Size(max = 500) String fileUrl,
        @Size(max = 500) String content
) {
}
