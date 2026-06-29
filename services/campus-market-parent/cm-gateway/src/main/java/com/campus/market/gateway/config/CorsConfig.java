package com.campus.market.gateway.config;

/**
 * CORS跨域配置
 * 
 * 注意：CORS配置已统一在application.yml的spring.cloud.gateway.globalcors中定义
 * 使用YAML配置的优势：
 * 1. 声明式配置，易于维护
 * 2. 支持allowedOriginPatterns通配符（如 http://localhost:*）
 * 3. 避免Java配置与YAML配置冲突
 * 
 * YAML配置位置: application.yml -> spring.cloud.gateway.globalcors
 */
// @Configuration  // 已禁用Java配置，使用YAML配置
public class CorsConfig {
    
    // CORS配置已移至application.yml
    // 保留此类作为文档说明
    
}
