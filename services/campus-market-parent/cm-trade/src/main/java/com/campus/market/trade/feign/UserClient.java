package com.campus.market.trade.feign;

import com.campus.market.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "cm-auth", url = "${feign.client.cm-auth.url:http://localhost:9001}")
public interface UserClient {
    
    @PostMapping("/internal/users/briefs")
    ApiResponse<List<Map<String, Object>>> getUserBriefs(@RequestBody Map<String, List<Long>> request);
    
    @GetMapping("/internal/users/{id}/status")
    ApiResponse<Map<String, Object>> getUserStatus(@PathVariable("id") Long id);
    
    @PutMapping("/internal/users/{id}/reputation")
    ApiResponse<Void> updateReputation(@PathVariable("id") Long id, @RequestParam("delta") int delta);
}
