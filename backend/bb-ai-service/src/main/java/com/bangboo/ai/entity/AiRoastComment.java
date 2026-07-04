package com.bangboo.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * AI 点评（ai_roast_comment，对齐需求文档 cq_ai_roast_comment）。
 * 存储 AI 生成的幽默点评，仅用于辅助理解与演示，不作为裁决依据。
 */
@Entity
@Table(
        name = "ai_roast_comment",
        indexes = {
                @Index(name = "idx_ai_roast_case", columnList = "case_id"),
                @Index(name = "idx_ai_roast_style", columnList = "style")
        }
)
public class AiRoastComment {
    @Id
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    /** 点评风格：OBJECTIVE / ROAST / TEACHER。 */
    @Column(nullable = false, length = 32)
    private String style;

    @Lob
    @Column(name = "comment_text", nullable = false, columnDefinition = "LONGTEXT")
    private String commentText;

    @Column(name = "safety_status", nullable = false, length = 20)
    private String safetyStatus;

    @Column(name = "safety_reason", length = 500)
    private String safetyReason;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getSafetyStatus() {
        return safetyStatus;
    }

    public void setSafetyStatus(String safetyStatus) {
        this.safetyStatus = safetyStatus;
    }

    public String getSafetyReason() {
        return safetyReason;
    }

    public void setSafetyReason(String safetyReason) {
        this.safetyReason = safetyReason;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
