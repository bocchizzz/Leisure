package com.bangboo.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * AI 案件摘要（ai_court_summary，对齐需求文档 cq_ai_court_summary）。
 * 存储 AI 书记官生成的案件摘要、争议焦点、证据分析、初步建议（仅供参考）。
 */
@Entity
@Table(
        name = "ai_court_summary",
        indexes = {
                @Index(name = "idx_ai_sum_case", columnList = "case_id"),
                @Index(name = "idx_ai_sum_created", columnList = "created_at")
        }
)
public class AiCourtSummary {
    @Id
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Lob
    @Column(name = "summary_text", nullable = false, columnDefinition = "LONGTEXT")
    private String summaryText;

    @Lob
    @Column(name = "dispute_focus", columnDefinition = "LONGTEXT")
    private String disputeFocus;

    @Lob
    @Column(name = "evidence_analysis", columnDefinition = "LONGTEXT")
    private String evidenceAnalysis;

    @Lob
    @Column(name = "suggestion", columnDefinition = "LONGTEXT")
    private String suggestion;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "prompt_version", length = 50)
    private String promptVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private Long createdBy;

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

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public String getDisputeFocus() {
        return disputeFocus;
    }

    public void setDisputeFocus(String disputeFocus) {
        this.disputeFocus = disputeFocus;
    }

    public String getEvidenceAnalysis() {
        return evidenceAnalysis;
    }

    public void setEvidenceAnalysis(String evidenceAnalysis) {
        this.evidenceAnalysis = evidenceAnalysis;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}
