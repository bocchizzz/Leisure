package com.campus.market.trade.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单请求
 */
@Data
public class CreateOrderRequest {
    
    @NotNull(message = "商品ID不能为空")
    private Long productId;
}
