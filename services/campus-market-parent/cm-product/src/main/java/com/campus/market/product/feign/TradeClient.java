package com.campus.market.product.feign;

import com.campus.market.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * 交易服务Feign客户端
 * 用于对账定时任务反查订单状态
 */
@FeignClient(name = "cm-trade", url = "${feign.client.cm-trade.url:http://localhost:9003}")
public interface TradeClient {
    
    /**
     * 根据订单号查询订单（对账用）
     * 返回订单的 orderNo/status/productId
     */
    @GetMapping("/internal/orders/by-order-no/{orderNo}")
    ApiResponse<Map<String, Object>> getOrderByOrderNo(@PathVariable("orderNo") String orderNo);
    
    /**
     * 获取所有订单列表（供AI使用）
     */
    @GetMapping("/internal/orders/all")
    ApiResponse<List<Map<String, Object>>> getAllOrders();
}
