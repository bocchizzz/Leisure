package com.campus.market.product.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.product.repository.ProductRepository;
import com.campus.market.product.enums.ProductStatus;
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
    
    private final ProductRepository productRepository;
    
    @GetMapping("/products")
    public ApiResponse<Map<String, Long>> getProductStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", productRepository.count());
        stats.put("available", productRepository.countByStatus(ProductStatus.AVAILABLE));
        return ApiResponse.success(stats);
    }
}
