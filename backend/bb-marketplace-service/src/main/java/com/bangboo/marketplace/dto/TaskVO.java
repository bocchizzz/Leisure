package com.bangboo.marketplace.dto;

import com.bangboo.marketplace.enums.BountyType;
import com.bangboo.marketplace.enums.SafetyStatus;
import com.bangboo.marketplace.enums.TaskCategory;
import com.bangboo.marketplace.enums.TaskStatus;

import java.time.Instant;
import java.util.List;

/** 任务视图对象，字段严格对齐前端 types/task.ts TaskVO。 */
public record TaskVO(
        Long id,
        String title,
        String description,
        TaskCategory category,
        String categoryName,
        String difficulty,
        Double bountyAmount,
        BountyType bountyType,
        String location,
        Instant deadline,
        String completionStandard,
        String evidenceRequirement,
        TaskStatus status,
        String statusName,
        Long publisherId,
        String publisherName,
        String publisherAvatar,
        Long assignedHunterId,
        String coverImageUrl,
        Integer applicationCount,
        Boolean isFavorited,
        Integer viewCount,
        SafetyStatus safetyStatus,
        String safetyReason,
        List<String> safetyLabels,
        Double safetyScore,
        Instant createdAt,
        Instant publishedAt
) {
}
