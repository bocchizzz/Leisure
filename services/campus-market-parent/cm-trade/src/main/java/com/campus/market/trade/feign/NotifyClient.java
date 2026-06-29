package com.campus.market.trade.feign;

import com.campus.market.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 通知服务Feign客户端
 */
@FeignClient(name = "cm-notify", url = "${feign.client.cm-notify.url:http://localhost:9004}")
public interface NotifyClient {
    
    @PostMapping("/internal/messages")
    ApiResponse<Void> sendMessage(@RequestBody Map<String, Object> message);
}
