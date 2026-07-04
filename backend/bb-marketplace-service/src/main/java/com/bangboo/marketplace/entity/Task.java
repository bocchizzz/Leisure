package com.bangboo.marketplace.entity;

import com.bangboo.marketplace.enums.BountyType;
import com.bangboo.marketplace.enums.SafetyStatus;
import com.bangboo.marketplace.enums.TaskCategory;
import com.bangboo.marketplace.enums.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(
        name = "mkt_task",
        indexes = {
                @Index(name = "idx_mkt_task_status", columnList = "status"),
                @Index(name = "idx_mkt_task_publisher", columnList = "publisher_id"),
                @Index(name = "idx_mkt_task_hunter", columnList = "assigned_hunter_id"),
                @Index(name = "idx_mkt_task_category", columnList = "category"),
                @Index(name = "idx_mkt_task_created", columnList = "created_at")
        }
)
public class Task {
    @Id
    private Long id;

    @Column(name = "task_no", length = 50)
    private String taskNo;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskCategory category = TaskCategory.OTHER;

    @Column(length = 4)
    private String difficulty;

    @Column(name = "bounty_amount", nullable = false)
    private Double bountyAmount = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "bounty_type", nullable = false, length = 20)
    private BountyType bountyType = BountyType.POINT;

    @Column(length = 200)
    private String location;

    @Column
    private Instant deadline;

    @Column(name = "completion_standard", length = 1000)
    private String completionStandard;

    @Column(name = "evidence_requirement", length = 1000)
    private String evidenceRequirement;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TaskStatus status = TaskStatus.PENDING_REVIEW;

    @Column(name = "publisher_id", nullable = false)
    private Long publisherId;

    @Column(name = "publisher_name", length = 80)
    private String publisherName;

    @Column(name = "publisher_avatar", length = 500)
    private String publisherAvatar;

    @Column(name = "assigned_hunter_id")
    private Long assignedHunterId;

    @Column(name = "application_count", nullable = false)
    private Integer applicationCount = 0;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "safety_status", nullable = false, length = 20)
    private SafetyStatus safetyStatus = SafetyStatus.PASS;

    @Column(name = "safety_score")
    private Double safetyScore;

    @Column(name = "safety_reason", length = 500)
    private String safetyReason;

    /** 逗号分隔存储，返回时转数组。 */
    @Column(name = "safety_labels", length = 500)
    private String safetyLabels;

    @Column(name = "review_comment", length = 500)
    private String reviewComment;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Integer version;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(String taskNo) {
        this.taskNo = taskNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskCategory getCategory() {
        return category;
    }

    public void setCategory(TaskCategory category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Double getBountyAmount() {
        return bountyAmount;
    }

    public void setBountyAmount(Double bountyAmount) {
        this.bountyAmount = bountyAmount;
    }

    public BountyType getBountyType() {
        return bountyType;
    }

    public void setBountyType(BountyType bountyType) {
        this.bountyType = bountyType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public void setDeadline(Instant deadline) {
        this.deadline = deadline;
    }

    public String getCompletionStandard() {
        return completionStandard;
    }

    public void setCompletionStandard(String completionStandard) {
        this.completionStandard = completionStandard;
    }

    public String getEvidenceRequirement() {
        return evidenceRequirement;
    }

    public void setEvidenceRequirement(String evidenceRequirement) {
        this.evidenceRequirement = evidenceRequirement;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getPublisherAvatar() {
        return publisherAvatar;
    }

    public void setPublisherAvatar(String publisherAvatar) {
        this.publisherAvatar = publisherAvatar;
    }

    public Long getAssignedHunterId() {
        return assignedHunterId;
    }

    public void setAssignedHunterId(Long assignedHunterId) {
        this.assignedHunterId = assignedHunterId;
    }

    public Integer getApplicationCount() {
        return applicationCount;
    }

    public void setApplicationCount(Integer applicationCount) {
        this.applicationCount = applicationCount;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public SafetyStatus getSafetyStatus() {
        return safetyStatus;
    }

    public void setSafetyStatus(SafetyStatus safetyStatus) {
        this.safetyStatus = safetyStatus;
    }

    public Double getSafetyScore() {
        return safetyScore;
    }

    public void setSafetyScore(Double safetyScore) {
        this.safetyScore = safetyScore;
    }

    public String getSafetyReason() {
        return safetyReason;
    }

    public void setSafetyReason(String safetyReason) {
        this.safetyReason = safetyReason;
    }

    public String getSafetyLabels() {
        return safetyLabels;
    }

    public void setSafetyLabels(String safetyLabels) {
        this.safetyLabels = safetyLabels;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
