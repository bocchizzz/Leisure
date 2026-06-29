package com.campus.market.trade.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建评价请求
 */
@Data
public class CreateReviewRequest {
    
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1分")
    @Max(value = 5, message = "评分最高5分")
    private Integer rating;
    
    private String content;
}
