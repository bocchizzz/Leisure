package com.campus.market.trade.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.trade.dto.OrderVO;
import com.campus.market.trade.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员订单控制器
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理员订单接口", description = "管理员订单管理")
public class AdminOrderController {
    
    private final OrderService orderService;
    
    @GetMapping
    @Operation(summary = "订单列表(管理员)")
    public ApiResponse<Page<OrderVO>> listAllOrders(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderVO> orders = orderService.listAllOrders(pageable);
        return ApiResponse.success(orders);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "订单详情(管理员)")
    public ApiResponse<OrderVO> getOrderDetail(@PathVariable Long id) {
        OrderVO order = orderService.getOrderByIdForAdmin(id);
        return ApiResponse.success(order);
    }
}
