package com.bangboo.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * AI 任务建议（ai_task_suggestion，对齐需求文档 cq_ai_task_suggestion）。
 * 存储任务拆解、分类推荐、赏金建议、风险提示及原始结构化结果。
 */
@Entity
@Table(
        name = "ai_task_suggestion",
        indexes = {
                @Index(name = "idx_ai_sug_user", columnList = "user_id"),
                @Index(name = "idx_ai_sug_task", columnList = "task_id")
        }
)
public class AiTaskSuggestion {
    @Id
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "task_id")
    private Long taskId;

    @Lob
    @Column(name = "input_text", nullable = false, columnDefinition = "LONGTEXT")
    private String inputText;

    @Column(name = "suggested_title", length = 100)
    private String suggestedTitle;

    @Column(name = "suggested_category", length = 32)
    private String suggestedCategory;

    @Column(name = "suggested_difficulty", length = 10)
    private String suggestedDifficulty;

    @Column(name = "suggested_bounty_min")
    private Integer suggestedBountyMin;

    @Column(name = "suggested_bounty_max")
    private Integer suggestedBountyMax;

    @Lob
    @Column(name = "task_steps", columnDefinition = "LONGTEXT")
    private String taskSteps;

    @Lob
    @Column(name = "risk_tips", columnDefinition = "LONGTEXT")
    private String riskTips;

    @Lob
    @Column(name = "ai_result_json", columnDefinition = "LONGTEXT")
    private String aiResultJson;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public String getSuggestedTitle() {
        return suggestedTitle;
    }

    public void setSuggestedTitle(String suggestedTitle) {
        this.suggestedTitle = suggestedTitle;
    }

    public String getSuggestedCategory() {
        return suggestedCategory;
    }

    public void setSuggestedCategory(String suggestedCategory) {
        this.suggestedCategory = suggestedCategory;
    }

    public String getSuggestedDifficulty() {
        return suggestedDifficulty;
    }

    public void setSuggestedDifficulty(String suggestedDifficulty) {
        this.suggestedDifficulty = suggestedDifficulty;
    }

    public Integer getSuggestedBountyMin() {
        return suggestedBountyMin;
    }

    public void setSuggestedBountyMin(Integer suggestedBountyMin) {
        this.suggestedBountyMin = suggestedBountyMin;
    }

    public Integer getSuggestedBountyMax() {
        return suggestedBountyMax;
    }

    public void setSuggestedBountyMax(Integer suggestedBountyMax) {
        this.suggestedBountyMax = suggestedBountyMax;
    }

    public String getTaskSteps() {
        return taskSteps;
    }

    public void setTaskSteps(String taskSteps) {
        this.taskSteps = taskSteps;
    }

    public String getRiskTips() {
        return riskTips;
    }

    public void setRiskTips(String riskTips) {
        this.riskTips = riskTips;
    }

    public String getAiResultJson() {
        return aiResultJson;
    }

    public void setAiResultJson(String aiResultJson) {
        this.aiResultJson = aiResultJson;
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
