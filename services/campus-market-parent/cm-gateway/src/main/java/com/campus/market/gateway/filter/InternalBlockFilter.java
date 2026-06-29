package com.campus.market.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 内部接口拦截过滤器（使用 WebFilter 确保在路由匹配之前执行）
 * 拒绝所有 /internal/** 请求，防止内部接口暴露到外部
 * 
 * 硬性约束 H0.1：内部接口只允许服务间调用，网关必须拒绝外部访问
 */
@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InternalBlockFilter implements WebFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 拦截所有 /internal/** 请求
        if (path.startsWith("/internal")) {
            log.warn("Blocked internal request from external: {} {}", 
                    request.getMethod(), path);
            
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            
            // 返回统一的 ApiResponse 格式
            String body = "{\"code\":403,\"message\":\"内部接口禁止外部访问\",\"data\":null}";
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        }
        
        return chain.filter(exchange);
    }
}
