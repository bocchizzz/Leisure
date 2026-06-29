package com.campus.market.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 请求ID过滤器（全局）
 */
@Component
public class RequestIdFilter implements GlobalFilter, Ordered {
    
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }
        
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(REQUEST_ID_HEADER, requestId)
                .build();
        
        // 在响应开始前添加头，避免干扰chunked响应流
        exchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, requestId);
        
        return chain.filter(exchange.mutate().request(request).build());
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
