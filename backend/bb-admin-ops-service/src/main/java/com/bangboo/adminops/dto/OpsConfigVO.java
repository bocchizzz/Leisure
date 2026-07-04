package com.bangboo.adminops.dto;

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
}
