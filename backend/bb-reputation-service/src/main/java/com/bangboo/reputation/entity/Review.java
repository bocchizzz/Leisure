package com.bangboo.reputation.entity;

import com.bangboo.reputation.enums.ReviewRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * 评价（rep_review）。
 * 保存 reviewer/reviewee 快照，避免展示时跨服务查用户。
 * 唯一约束：同一契约同一评价角色只能有一条（防重复评价）。
 */
@Entity
@Table(
        name = "rep_review",
        uniqueConstraints = @UniqueConstraint(name = "uk_review_contract_role", columnNames = {"contract_id", "role"}),
        indexes = {
                @Index(name = "idx_review_reviewee", columnList = "reviewee_id"),
                @Index(name = "idx_review_task", columnList = "task_id"),
                @Index(name = "idx_review_contract", columnList = "contract_id")
        }
)
public class Review {
    @Id
    private Long id;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "task_title", length = 200)
    private String taskTitle;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Column(name = "reviewer_name", length = 100)
    private String reviewerName;

    @Column(name = "reviewer_avatar", length = 200)
    private String reviewerAvatar;

    @Column(name = "reviewee_id", nullable = false)
    private Long revieweeId;

    @Column(name = "reviewee_name", length = 100)
    private String revieweeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReviewRole role;

    @Column(nullable = false)
    private Integer rating;

    /** 逗号分隔的标签，VO 中转数组。 */
    @Column(length = 500)
    private String tags;

    @Column(length = 1000)
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getReviewerAvatar() {
        return reviewerAvatar;
    }

    public void setReviewerAvatar(String reviewerAvatar) {
        this.reviewerAvatar = reviewerAvatar;
    }

    public Long getRevieweeId() {
        return revieweeId;
    }

    public void setRevieweeId(Long revieweeId) {
        this.revieweeId = revieweeId;
    }

    public String getRevieweeName() {
        return revieweeName;
    }

    public void setRevieweeName(String revieweeName) {
        this.revieweeName = revieweeName;
    }

    public ReviewRole getRole() {
        return role;
    }

    public void setRole(ReviewRole role) {
        this.role = role;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
