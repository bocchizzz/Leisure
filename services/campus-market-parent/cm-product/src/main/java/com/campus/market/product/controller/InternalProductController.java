package com.campus.market.product.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 内部商品接口（供其他微服务调用）
 * 必须校验 X-Internal-Token
 */
@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
@Tag(name = "内部商品接口", description = "供其他微服务调用")
public class InternalProductController {
    
    private final ProductService productService;
    
    @PostMapping("/{id}/reserve")
    @Operation(summary = "预占商品")
    public ApiResponse<Boolean> reserveProduct(
            @PathVariable Long id,
            @RequestParam String orderNo) {
        boolean success = productService.reserveProduct(id, orderNo);
        return ApiResponse.success(success);
    }
    
    @PostMapping("/{id}/confirm-reserve")
    @Operation(summary = "确认预占")
    public ApiResponse<Boolean> confirmReserve(
            @PathVariable Long id,
            @RequestParam String orderNo) {
        boolean success = productService.confirmReserve(id, orderNo);
        return ApiResponse.success(success);
    }
    
    @PostMapping("/{id}/cancel-reserve")
    @Operation(summary = "取消预占")
    public ApiResponse<Boolean> cancelReserve(
            @PathVariable Long id,
            @RequestParam String orderNo) {
        boolean success = productService.cancelReserve(id, orderNo);
        return ApiResponse.success(success);
    }
    
    @PostMapping("/{id}/mark-sold")
    @Operation(summary = "标记已售出")
    public ApiResponse<Boolean> markSold(
            @PathVariable Long id,
            @RequestParam String orderNo) {
        boolean success = productService.markSold(id, orderNo);
        return ApiResponse.success(success);
    }
    
    @PostMapping("/briefs")
    @Operation(summary = "批量获取商品简要信息")
    public ApiResponse<List<Map<String, Object>>> getProductBriefs(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        List<Map<String, Object>> briefs = productService.getProductBriefs(ids);
        return ApiResponse.success(briefs);
    }
}
