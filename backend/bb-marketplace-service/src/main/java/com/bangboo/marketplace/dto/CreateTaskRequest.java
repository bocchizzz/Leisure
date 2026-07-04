package com.bangboo.marketplace.dto;

import com.bangboo.marketplace.enums.BountyType;
import com.bangboo.marketplace.enums.TaskCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/** 发布/编辑任务请求，字段对齐前端 CreateTaskRequest。 */
public record CreateTaskRequest(
        @NotBlank(message = "任务标题不能为空") String title,
        @NotBlank(message = "任务描述不能为空") String description,
        @NotNull(message = "任务分类不能为空") TaskCategory category,
        @NotBlank(message = "任务难度不能为空") String difficulty,
        @NotNull(message = "赏金金额不能为空") Double bountyAmount,
        @NotNull(message = "赏金类型不能为空") BountyType bountyType,
        String location,
        Instant deadline,
        String completionStandard,
        String evidenceRequirement,
        String coverImageUrl
) {
}
