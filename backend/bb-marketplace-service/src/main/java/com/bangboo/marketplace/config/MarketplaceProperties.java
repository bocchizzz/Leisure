package com.bangboo.marketplace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bb.marketplace")
public class MarketplaceProperties {
    private String internalToken = "bangboo-internal-dev-token";
    private String iamBaseUrl = "http://127.0.0.1:8081";
    private String messageBaseUrl = "http://127.0.0.1:8084";
    private String adminOpsBaseUrl = "http://127.0.0.1:8088";
    private String reputationBaseUrl = "http://127.0.0.1:8083";

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

    public String getMessageBaseUrl() {
        return messageBaseUrl;
    }

    public void setMessageBaseUrl(String messageBaseUrl) {
        this.messageBaseUrl = messageBaseUrl;
    }

    public String getAdminOpsBaseUrl() {
        return adminOpsBaseUrl;
    }

    public void setAdminOpsBaseUrl(String adminOpsBaseUrl) {
        this.adminOpsBaseUrl = adminOpsBaseUrl;
    }

    public String getReputationBaseUrl() {
        return reputationBaseUrl;
    }

    public void setReputationBaseUrl(String reputationBaseUrl) {
        this.reputationBaseUrl = reputationBaseUrl;
    }
}
