package com.bangboo.reputation.dto;

import com.bangboo.reputation.enums.ReviewRole;

import java.time.Instant;
import java.util.List;

/** 评价视图对象。字段对齐前端 review.ts ReviewVO。 */
public record ReviewVO(
        Long id,
        Long contractId,
        Long taskId,
        String taskTitle,
        Long reviewerId,
        String reviewerName,
        String reviewerAvatar,
        Long revieweeId,
        String revieweeName,
        ReviewRole role,
        Integer rating,
        List<String> tags,
        String content,
        Instant createdAt
) {
}
