package com.bangboo.adminops.service;

import com.bangboo.adminops.dto.OpsConfigVO;
import com.bangboo.adminops.entity.OpsConfigEntity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class OpsConfigMapper {
    private OpsConfigMapper() {
    }

    public static OpsConfigVO toVO(OpsConfigEntity entity) {
        return new OpsConfigVO(
                entity.getTaskReviewMode(),
                entity.getMinAutoPassSafetyScore(),
                entity.getMaxAutoBlockSafetyScore(),
                entity.isAiSafetyEnabled(),
                entity.isAiOutputWatermark(),
                split(entity.getBannedKeywords()),
                entity.getFileMaxSizeMb(),
                split(entity.getAllowedFileTypes()),
                entity.getJuryMinReputation(),
                entity.getJuryMinCompletedTasks(),
                entity.getVoteQuorum(),
                entity.getDisputeAutoEscalationHours(),
                entity.getUpdatedAt()
        );
    }

    public static String join(List<String> values) {
        if (values == null) {
            return "";
        }
        return values.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(","));
    }

    private static List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }
}
