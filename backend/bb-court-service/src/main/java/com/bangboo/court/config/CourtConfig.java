package com.bangboo.court.config;

import com.bangboo.common.security.InternalTokenFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(CourtProperties.class)
public class CourtConfig {
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    /**
     * 保护内部接口 /internal/*，仅允许携带正确 X-Internal-Token 的服务间调用。
     * 仅拦截 /internal/*，不影响用户态接口。
     */
    @Bean
    public FilterRegistrationBean<InternalTokenFilter> internalTokenFilterRegistration(CourtProperties properties) {
        FilterRegistrationBean<InternalTokenFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new InternalTokenFilter(properties.getInternalToken()));
        registration.setOrder(-80);
        registration.addUrlPatterns("/internal/*");
        return registration;
    }
}

