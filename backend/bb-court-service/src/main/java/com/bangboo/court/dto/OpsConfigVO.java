package com.bangboo.court.dto;

import java.time.Instant;
import java.util.List;

public record OpsConfigVO(
        String taskReviewMode,
        int minAutoPassSafetyScore,
        int maxAutoBlockSafetyScore,
        boolean aiSafetyEnabled,
        boolean aiOutputWatermark,
        List<String> bannedKeywords,
        int fileMaxSizeMb,
        List<String> allowedFileTypes,
        int juryMinReputation,
        int juryMinCompletedTasks,
        int voteQuorum,
        int disputeAutoEscalationHours,
        Instant updatedAt
) {
    public static OpsConfigVO defaults() {
        return new OpsConfigVO(
                "HYBRID",
                45,
                90,
                true,
                true,
                List.of(),
                8,
                List.of("image/png", "image/jpeg", "image/webp", "application/pdf"),
                750,
                3,
                5,
                24,
                Instant.now()
        );
    }
}
