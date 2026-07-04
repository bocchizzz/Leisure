package com.bangboo.reputation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bb.reputation")
public class ReputationProperties {
    private String internalToken = "bangboo-internal-dev-token";
    private String iamBaseUrl = "http://127.0.0.1:8081";
    private String marketplaceBaseUrl = "http://127.0.0.1:8082";
    private String messageBaseUrl = "http://127.0.0.1:8084";

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

    public String getMessageBaseUrl() {
        return messageBaseUrl;
    }

    public void setMessageBaseUrl(String messageBaseUrl) {
        this.messageBaseUrl = messageBaseUrl;
    }
}
