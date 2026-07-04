package com.bangboo.reputation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** 提交评价请求。字段对齐前端 review.ts CreateReviewRequest。评价方向由后端根据当前用户与契约双方判断。 */
public record CreateReviewRequest(
        @NotNull(message = "契约ID不能为空") Long contractId,
        @NotNull(message = "评分不能为空") @Min(value = 1, message = "评分最低1分") @Max(value = 5, message = "评分最高5分") Integer rating,
        List<String> tags,
        String content
) {
}
