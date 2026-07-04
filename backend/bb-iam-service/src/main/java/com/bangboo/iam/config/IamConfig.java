package com.bangboo.iam.config;

import com.bangboo.common.security.InternalTokenFilter;
import com.bangboo.common.security.JwtAuthenticationFilter;
import com.bangboo.common.security.JwtTokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(IamProperties.class)
public class IamConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider(IamProperties properties) {
        return new JwtTokenProvider(properties.getJwtSecret(), properties.getJwtTtl());
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtTokenProvider jwtTokenProvider
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtAuthenticationFilter(jwtTokenProvider));
        registration.setOrder(-90);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<InternalTokenFilter> internalTokenFilterRegistration(IamProperties properties) {
        FilterRegistrationBean<InternalTokenFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new InternalTokenFilter(properties.getInternalToken()));
        registration.setOrder(-80);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
