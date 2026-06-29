package com.campus.market.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * 认证过滤器
 * 仅做简单的Token存在性校验，具体验证由下游服务完成
 */
@Component
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    // 白名单路径（无需认证）
    private final List<String> whitelist = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/products",
            "/api/products/{id}",
            "/api/products/recommend",
            "/api/products/recommendations",
            "/api/products/price-suggestion",
            "/api/users/{id}",
            "/api/users/{id}/reputation-report",
            "/api/reviews/seller/**",
            "/api/reviews/product/**",
            "/api/reviews/order/**",
            "/api/stats/**",
            "/uploads/**",
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        
        // OPTIONS预检请求直接放行（CORS需要）
        if ("OPTIONS".equals(method)) {
            return chain.filter(exchange);
        }
        
        // GET请求的商品列表和详情是公开的
        if ("GET".equals(method) && isWhitelisted(path)) {
            return chain.filter(exchange);
        }
        
        // 非GET请求检查白名单
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }
        
        // 检查Authorization头
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return writeErrorResponse(exchange, HttpStatus.FORBIDDEN, "请先登录");
        }
        
        // Token存在，放行到下游服务做详细验证
        return chain.filter(exchange);
    }
    
    private boolean isWhitelisted(String path) {
        return whitelist.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
    
    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"code\":%d,\"message\":\"%s\",\"data\":null}", status.value(), message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
