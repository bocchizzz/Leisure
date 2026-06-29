package com.campus.market.product.feign;

import com.campus.market.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/**
 * 通知服务Feign客户端
 * 用于获取申诉等数据
 */
@FeignClient(name = "cm-notify", url = "${feign.client.cm-notify.url:http://localhost:9004}")
public interface NotifyClient {
    
    /**
     * 获取所有申诉列表（供AI使用）
     */
    @GetMapping("/internal/appeals/all")
    ApiResponse<List<Map<String, Object>>> getAllAppeals();
}
