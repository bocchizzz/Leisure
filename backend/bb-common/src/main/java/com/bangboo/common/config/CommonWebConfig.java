package com.bangboo.common.config;

import com.bangboo.common.filter.RequestIdFilter;
import com.bangboo.common.security.HeaderAuthenticationFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonWebConfig {
    @Bean
    public FilterRegistrationBean<RequestIdFilter> requestIdFilterRegistration() {
        FilterRegistrationBean<RequestIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestIdFilter());
        registration.setOrder(-100);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<HeaderAuthenticationFilter> headerAuthenticationFilterRegistration() {
        FilterRegistrationBean<HeaderAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new HeaderAuthenticationFilter());
        registration.setOrder(-95);
        return registration;
    }
}
