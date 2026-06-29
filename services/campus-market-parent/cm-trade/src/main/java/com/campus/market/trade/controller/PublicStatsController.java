package com.campus.market.trade.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.trade.repository.OrderRepository;
import com.campus.market.trade.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class PublicStatsController {
    
    private final OrderRepository orderRepository;
    
    @GetMapping("/orders")
    public ApiResponse<Map<String, Long>> getOrderStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", orderRepository.count());
        stats.put("pending", orderRepository.countByStatus(OrderStatus.PENDING));
        stats.put("completed", orderRepository.countByStatus(OrderStatus.COMPLETED));
        return ApiResponse.success(stats);
    }
}
