package com.campus.market.trade.feign;

import com.campus.market.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品服务Feign客户端
 */
@FeignClient(name = "cm-product", url = "${feign.client.cm-product.url:http://localhost:9002}")
public interface ProductClient {
    
    @PostMapping("/internal/products/{id}/reserve")
    ApiResponse<Boolean> reserveProduct(@PathVariable("id") Long id, @RequestParam("orderNo") String orderNo);
    
    @PostMapping("/internal/products/{id}/confirm-reserve")
    ApiResponse<Boolean> confirmReserve(@PathVariable("id") Long id, @RequestParam("orderNo") String orderNo);
    
    @PostMapping("/internal/products/{id}/cancel-reserve")
    ApiResponse<Boolean> cancelReserve(@PathVariable("id") Long id, @RequestParam("orderNo") String orderNo);
    
    @PostMapping("/internal/products/{id}/mark-sold")
    ApiResponse<Boolean> markSold(@PathVariable("id") Long id, @RequestParam("orderNo") String orderNo);
    
    @PostMapping("/internal/products/briefs")
    ApiResponse<List<Map<String, Object>>> getProductBriefs(@RequestBody Map<String, List<Long>> request);
    
    @GetMapping("/api/products/{id}")
    ApiResponse<Map<String, Object>> getProduct(@PathVariable("id") Long id);
}
