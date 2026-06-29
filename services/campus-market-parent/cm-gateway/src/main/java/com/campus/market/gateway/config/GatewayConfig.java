package com.campus.market.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关路由配置
 */
// @Configuration  // 禁用Java路由配置，使用YAML配置
public class GatewayConfig {
    
    // 路由配置已移至 application.yml
    // @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 认证服务路由
                .route("cm-auth", r -> r
                        .path("/api/auth/**", "/api/users/**", "/api/admin/users/**")
                        .uri("http://localhost:9001"))
                
                // 商品服务路由
                .route("cm-product", r -> r
                        .path("/api/products/**", "/api/favorites/**", "/api/history/**", "/api/browse-history/**")
                        .uri("http://localhost:9002"))
                
                // 交易服务路由
                .route("cm-trade", r -> r
                        .path("/api/orders/**", "/api/reviews/**")
                        .uri("http://localhost:9003"))
                
                // 通知服务路由
                .route("cm-notify", r -> r
                        .path("/api/messages/**", "/api/appeals/**", "/api/admin/appeals/**", "/api/admin/audit-logs/**")
                        .uri("http://localhost:9004"))
                
                // 文件上传（暂时路由到商品服务）
                .route("files", r -> r
                        .path("/api/files/**")
                        .uri("http://localhost:9002"))
                
                .build();
    }
}
