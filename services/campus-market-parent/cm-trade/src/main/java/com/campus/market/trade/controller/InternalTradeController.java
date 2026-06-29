package com.campus.market.trade.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.trade.entity.Order;
import com.campus.market.trade.repository.OrderRepository;
import com.campus.market.trade.repository.ReviewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 内部交易接口（供其他微服务调用）
 * 主要用于 cm-product 的对账定时任务
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Tag(name = "内部交易接口", description = "供其他微服务调用")
public class InternalTradeController {
    
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    
    /**
     * 根据订单号查询订单（对账用）
     * cm-product 定时任务用于判断悬挂预占是否应该释放
     */
    @GetMapping("/orders/by-order-no/{orderNo}")
    @Operation(summary = "根据订单号查询订单")
    public ApiResponse<Map<String, Object>> getOrderByOrderNo(@PathVariable String orderNo) {
        Optional<Order> orderOpt = orderRepository.findByOrderNo(orderNo);
        
        if (orderOpt.isEmpty()) {
            return ApiResponse.error(404, "订单不存在");
        }
        
        Order order = orderOpt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("status", order.getStatus().name());
        result.put("productId", order.getProductId());
        result.put("buyerId", order.getBuyerId());
        result.put("sellerId", order.getSellerId());
        
        return ApiResponse.success(result);
    }
    
    /**
     * 获取卖家平均评分（内部接口）
     */
    @GetMapping("/reviews/seller/{sellerId}/average-rating")
    @Operation(summary = "获取卖家平均评分")
    public ApiResponse<Double> getSellerAverageRating(@PathVariable Long sellerId) {
        Double avgRating = reviewRepository.getAverageRatingBySellerId(sellerId);
        return ApiResponse.success(avgRating != null ? avgRating : 0.0);
    }
    
    /**
     * 批量获取订单简要信息
     */
    @PostMapping("/orders/briefs")
    @Operation(summary = "批量获取订单简要信息")
    public ApiResponse<List<Map<String, Object>>> getOrderBriefs(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ApiResponse.success(List.of());
        }
        
        List<Order> orders = orderRepository.findAllById(ids);
        List<Map<String, Object>> result = orders.stream().map(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("status", order.getStatus().name());
            map.put("productId", order.getProductId());
            map.put("buyerId", order.getBuyerId());
            map.put("sellerId", order.getSellerId());
            return map;
        }).toList();
        
        return ApiResponse.success(result);
    }
    
    /**
     * 获取所有订单列表（供AI使用）
     */
    @GetMapping("/orders/all")
    @Operation(summary = "获取所有订单列表")
    public ApiResponse<List<Map<String, Object>>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<Map<String, Object>> result = orders.stream().map(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("status", order.getStatus().name());
            map.put("statusName", getOrderStatusName(order.getStatus().name()));
            map.put("productId", order.getProductId());
            map.put("productTitle", order.getProductTitle());
            map.put("buyerId", order.getBuyerId());
            map.put("sellerId", order.getSellerId());
            map.put("totalPrice", order.getTotalPrice());
            map.put("reviewed", order.getReviewed());
            map.put("createdAt", order.getCreatedAt());
            return map;
        }).toList();
        
        return ApiResponse.success(result);
    }
    
    private String getOrderStatusName(String status) {
        switch (status) {
            case "PENDING": return "待确认";
            case "CONFIRMED": return "已确认";
            case "SHIPPED": return "已发货";
            case "COMPLETED": return "已完成";
            case "CANCELLED": return "已取消";
            default: return status;
        }
    }
}
