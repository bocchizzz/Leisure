package com.bangboo.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * AI 调用日志（ai_call_log，对齐需求文档 cq_ai_call_log）。
 * 记录每次 AI 能力调用的模型、token、耗时、状态，供统计/排查/成本估算。
 */
@Entity
@Table(
        name = "ai_call_log",
        indexes = {
                @Index(name = "idx_ai_log_user", columnList = "user_id"),
                @Index(name = "idx_ai_log_biz", columnList = "business_type"),
                @Index(name = "idx_ai_log_status", columnList = "status")
        }
)
public class AiCallLog {
    @Id
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    /** 业务类型：TASK_SUGGESTION / BOUNTY / RISK_CHECK / COURT_SUMMARY / ROAST_COMMENT / CHAT / SMART_SEARCH / COVER / AVATAR。 */
    @Column(name = "business_type", nullable = false, length = 32)
    private String businessType;

    @Column(name = "related_type", length = 32)
    private String relatedType;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "prompt_version", length = 50)
    private String promptVersion;

    @Column(name = "request_tokens")
    private Integer requestTokens;

    @Column(name = "response_tokens")
    private Integer responseTokens;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    /** SUCCESS / FAIL / TIMEOUT / DEGRADED（降级到规则生成）。 */
    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getRelatedType() {
        return relatedType;
    }

    public void setRelatedType(String relatedType) {
        this.relatedType = relatedType;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }

    public Integer getRequestTokens() {
        return requestTokens;
    }

    public void setRequestTokens(Integer requestTokens) {
        this.requestTokens = requestTokens;
    }

    public Integer getResponseTokens() {
        return responseTokens;
    }

    public void setResponseTokens(Integer responseTokens) {
        this.responseTokens = responseTokens;
    }

    public Integer getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Integer latencyMs) {
        this.latencyMs = latencyMs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
