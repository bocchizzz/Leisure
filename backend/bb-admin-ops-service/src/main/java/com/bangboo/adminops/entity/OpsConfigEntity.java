package com.bangboo.adminops.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "ops_config")
public class OpsConfigEntity {
    @Id
    private Long id = 1L;

    @Column(name = "task_review_mode", nullable = false, length = 20)
    private String taskReviewMode = "HYBRID";

    @Column(name = "min_auto_pass_safety_score", nullable = false)
    private int minAutoPassSafetyScore = 45;

    @Column(name = "max_auto_block_safety_score", nullable = false)
    private int maxAutoBlockSafetyScore = 90;

    @Column(name = "ai_safety_enabled", nullable = false)
    private boolean aiSafetyEnabled = true;

    @Column(name = "ai_output_watermark", nullable = false)
    private boolean aiOutputWatermark = true;

    @Column(name = "banned_keywords", length = 1000)
    private String bannedKeywords = "代写论文,私下转账,买卖账号";

    @Column(name = "file_max_size_mb", nullable = false)
    private int fileMaxSizeMb = 8;

    @Column(name = "allowed_file_types", length = 1000)
    private String allowedFileTypes = "image/png,image/jpeg,image/webp,application/pdf";

    @Column(name = "jury_min_reputation", nullable = false)
    private int juryMinReputation = 750;

    @Column(name = "jury_min_completed_tasks", nullable = false)
    private int juryMinCompletedTasks = 3;

    @Column(name = "vote_quorum", nullable = false)
    private int voteQuorum = 5;

    @Column(name = "dispute_auto_escalation_hours", nullable = false)
    private int disputeAutoEscalationHours = 24;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getTaskReviewMode() {
        return taskReviewMode;
    }

    public void setTaskReviewMode(String taskReviewMode) {
        this.taskReviewMode = taskReviewMode;
    }

    public int getMinAutoPassSafetyScore() {
        return minAutoPassSafetyScore;
    }

    public void setMinAutoPassSafetyScore(int minAutoPassSafetyScore) {
        this.minAutoPassSafetyScore = minAutoPassSafetyScore;
    }

    public int getMaxAutoBlockSafetyScore() {
        return maxAutoBlockSafetyScore;
    }

    public void setMaxAutoBlockSafetyScore(int maxAutoBlockSafetyScore) {
        this.maxAutoBlockSafetyScore = maxAutoBlockSafetyScore;
    }

    public boolean isAiSafetyEnabled() {
        return aiSafetyEnabled;
    }

    public void setAiSafetyEnabled(boolean aiSafetyEnabled) {
        this.aiSafetyEnabled = aiSafetyEnabled;
    }

    public boolean isAiOutputWatermark() {
        return aiOutputWatermark;
    }

    public void setAiOutputWatermark(boolean aiOutputWatermark) {
        this.aiOutputWatermark = aiOutputWatermark;
    }

    public String getBannedKeywords() {
        return bannedKeywords;
    }

    public void setBannedKeywords(String bannedKeywords) {
        this.bannedKeywords = bannedKeywords;
    }

    public int getFileMaxSizeMb() {
        return fileMaxSizeMb;
    }

    public void setFileMaxSizeMb(int fileMaxSizeMb) {
        this.fileMaxSizeMb = fileMaxSizeMb;
    }

    public String getAllowedFileTypes() {
        return allowedFileTypes;
    }

    public void setAllowedFileTypes(String allowedFileTypes) {
        this.allowedFileTypes = allowedFileTypes;
    }

    public int getJuryMinReputation() {
        return juryMinReputation;
    }

    public void setJuryMinReputation(int juryMinReputation) {
        this.juryMinReputation = juryMinReputation;
    }

    public int getJuryMinCompletedTasks() {
        return juryMinCompletedTasks;
    }

    public void setJuryMinCompletedTasks(int juryMinCompletedTasks) {
        this.juryMinCompletedTasks = juryMinCompletedTasks;
    }

    public int getVoteQuorum() {
        return voteQuorum;
    }

    public void setVoteQuorum(int voteQuorum) {
        this.voteQuorum = voteQuorum;
    }

    public int getDisputeAutoEscalationHours() {
        return disputeAutoEscalationHours;
    }

    public void setDisputeAutoEscalationHours(int disputeAutoEscalationHours) {
        this.disputeAutoEscalationHours = disputeAutoEscalationHours;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
