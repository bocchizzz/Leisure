package com.bangboo.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GatewaySecurityProperties.class)
public class GatewayConfig {
}
