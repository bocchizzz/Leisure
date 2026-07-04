package com.bangboo.adminops.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(AdminOpsProperties.class)
public class AdminOpsConfig {
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
