package com.bangboo.reputation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 信誉变化日志（rep_credit_log）。
 * 记录每次信誉分变更的前后值与来源。
 */
@Entity
@Table(
        name = "rep_credit_log",
        indexes = @Index(name = "idx_credit_user", columnList = "user_id")
)
public class CreditLog {
    @Id
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer delta;

    @Column(name = "before_score", nullable = false)
    private Integer beforeScore;

    @Column(name = "after_score", nullable = false)
    private Integer afterScore;

    @Column(name = "source_type", length = 64)
    private String sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(length = 500)
    private String reason;

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

    public Integer getDelta() {
        return delta;
    }

    public void setDelta(Integer delta) {
        this.delta = delta;
    }

    public Integer getBeforeScore() {
        return beforeScore;
    }

    public void setBeforeScore(Integer beforeScore) {
        this.beforeScore = beforeScore;
    }

    public Integer getAfterScore() {
        return afterScore;
    }

    public void setAfterScore(Integer afterScore) {
        this.afterScore = afterScore;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
