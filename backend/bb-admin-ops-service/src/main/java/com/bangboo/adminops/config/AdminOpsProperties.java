package com.bangboo.adminops.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bb.admin-ops")
public class AdminOpsProperties {
    private String internalToken = "bangboo-internal-dev-token";
    private String iamBaseUrl = "http://127.0.0.1:8081";
    private String marketplaceBaseUrl = "http://127.0.0.1:8082";
    private String reputationBaseUrl = "http://127.0.0.1:8083";
    private String aiBaseUrl = "http://127.0.0.1:8087";

    public String getInternalToken() {
        return internalToken;
    }

    public void setInternalToken(String internalToken) {
        this.internalToken = internalToken;
    }

    public String getIamBaseUrl() {
        return iamBaseUrl;
    }

    public void setIamBaseUrl(String iamBaseUrl) {
        this.iamBaseUrl = iamBaseUrl;
    }

    public String getMarketplaceBaseUrl() {
        return marketplaceBaseUrl;
    }

    public void setMarketplaceBaseUrl(String marketplaceBaseUrl) {
        this.marketplaceBaseUrl = marketplaceBaseUrl;
    }

    public String getReputationBaseUrl() {
        return reputationBaseUrl;
    }

    public void setReputationBaseUrl(String reputationBaseUrl) {
        this.reputationBaseUrl = reputationBaseUrl;
    }

    public String getAiBaseUrl() {
        return aiBaseUrl;
    }

    public void setAiBaseUrl(String aiBaseUrl) {
        this.aiBaseUrl = aiBaseUrl;
    }
}
