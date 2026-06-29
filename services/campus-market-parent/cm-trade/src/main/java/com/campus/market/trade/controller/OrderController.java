package com.campus.market.trade.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.common.security.SecurityUtils;
import com.campus.market.trade.dto.CreateOrderRequest;
import com.campus.market.trade.dto.OrderVO;
import com.campus.market.trade.enums.OrderStatus;
import com.campus.market.trade.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "订单接口", description = "订单管理")
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "创建订单")
    public ApiResponse<OrderVO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Long buyerId = SecurityUtils.getCurrentUserId();
        OrderVO order = orderService.createOrder(buyerId, request);
        return ApiResponse.success("下单成功", order);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "订单详情")
    public ApiResponse<OrderVO> getOrder(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        OrderVO order = orderService.getOrderById(id, userId);
        return ApiResponse.success(order);
    }
    
    @GetMapping("/buyer")
    @Operation(summary = "买家订单列表")
    public ApiResponse<Page<OrderVO>> getBuyerOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long buyerId = SecurityUtils.getCurrentUserId();
        Page<OrderVO> orders = orderService.getBuyerOrders(buyerId, status, pageable);
        return ApiResponse.success(orders);
    }
    
    @GetMapping("/seller")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "卖家订单列表")
    public ApiResponse<Page<OrderVO>> getSellerOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long sellerId = SecurityUtils.getCurrentUserId();
        Page<OrderVO> orders = orderService.getSellerOrders(sellerId, status, pageable);
        return ApiResponse.success(orders);
    }
    
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "确认订单")
    public ApiResponse<OrderVO> confirmOrder(@PathVariable Long id) {
        Long sellerId = SecurityUtils.getCurrentUserId();
        OrderVO order = orderService.confirmOrder(id, sellerId);
        return ApiResponse.success("订单已确认", order);
    }
    
    @PutMapping("/{id}/ship")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "发货")
    public ApiResponse<OrderVO> shipOrder(@PathVariable Long id) {
        Long sellerId = SecurityUtils.getCurrentUserId();
        OrderVO order = orderService.shipOrder(id, sellerId);
        return ApiResponse.success("已发货", order);
    }
    
    @PutMapping("/{id}/complete")
    @Operation(summary = "确认收货")
    public ApiResponse<OrderVO> completeOrder(@PathVariable Long id) {
        Long buyerId = SecurityUtils.getCurrentUserId();
        OrderVO order = orderService.completeOrder(id, buyerId);
        return ApiResponse.success("已确认收货", order);
    }
    
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public ApiResponse<OrderVO> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        // 优先使用body中的reason，兼容前端传参方式
        String actualReason = reason;
        if (actualReason == null && body != null) {
            actualReason = body.get("reason");
        }
        OrderVO order = orderService.cancelOrder(id, userId, actualReason);
        return ApiResponse.success("订单已取消", order);
    }
}
