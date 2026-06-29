package com.campus.market.auth.feign;

import com.campus.market.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 交易服务Feign客户端
 * 用于获取卖家平均评分等信息
 */
@FeignClient(name = "cm-trade", url = "${feign.client.cm-trade.url:http://localhost:9003}")
public interface TradeClient {
    
    /**
     * 获取卖家平均评分
     */
    @GetMapping("/internal/reviews/seller/{sellerId}/average-rating")
    ApiResponse<Double> getSellerAverageRating(@PathVariable("sellerId") Long sellerId);
}
