package com.bangboo.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bb.gateway.security")
public class GatewaySecurityProperties {
    private String jwtSecret = "bangboo-dev-secret-change-me-please-32";

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }
}
