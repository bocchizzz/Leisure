package com.campus.market.product.feign;

import com.campus.market.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 认证服务Feign客户端
 * 用于获取用户信息（如sellerName）
 */
@FeignClient(name = "cm-auth", url = "${feign.client.cm-auth.url:http://localhost:9001}")
public interface AuthClient {
    
    /**
     * 批量获取用户简要信息
     */
    @PostMapping("/internal/users/briefs")
    ApiResponse<List<Map<String, Object>>> getUserBriefs(@RequestBody Map<String, List<Long>> request);
    
    /**
     * 获取单个用户状态
     */
    @GetMapping("/internal/users/{id}/status")
    ApiResponse<Map<String, Object>> getUserStatus(@PathVariable("id") Long id);
    
    /**
     * 获取所有用户列表（供AI使用）
     */
    @GetMapping("/internal/users/all")
    ApiResponse<List<Map<String, Object>>> getAllUsers();
}
