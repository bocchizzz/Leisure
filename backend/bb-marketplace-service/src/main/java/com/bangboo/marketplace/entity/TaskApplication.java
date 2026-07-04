package com.bangboo.marketplace.entity;

import com.bangboo.marketplace.enums.ApplicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "mkt_task_application",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mkt_app_task_hunter", columnNames = {"task_id", "hunter_id"})
        },
        indexes = {
                @Index(name = "idx_mkt_app_task_status", columnList = "task_id,status"),
                @Index(name = "idx_mkt_app_hunter_status", columnList = "hunter_id,status"),
                @Index(name = "idx_mkt_app_publisher", columnList = "publisher_id")
        }
)
public class TaskApplication {
    @Id
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "task_title", length = 100)
    private String taskTitle;

    @Column(name = "hunter_id", nullable = false)
    private Long hunterId;

    @Column(name = "hunter_name", length = 80)
    private String hunterName;

    @Column(name = "hunter_avatar", length = 500)
    private String hunterAvatar;

    @Column(name = "hunter_level")
    private Integer hunterLevel;

    @Column(name = "hunter_title", length = 40)
    private String hunterTitle;

    @Column(name = "hunter_reputation")
    private Integer hunterReputation;

    @Column(name = "publisher_id", nullable = false)
    private Long publisherId;

    @Column(name = "apply_message", length = 1000)
    private String applyMessage;

    @Column(name = "expected_finish_time")
    private Instant expectedFinishTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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

    public Long getHunterId() {
        return hunterId;
    }

    public void setHunterId(Long hunterId) {
        this.hunterId = hunterId;
    }

    public String getHunterName() {
        return hunterName;
    }

    public void setHunterName(String hunterName) {
        this.hunterName = hunterName;
    }

    public String getHunterAvatar() {
        return hunterAvatar;
    }

    public void setHunterAvatar(String hunterAvatar) {
        this.hunterAvatar = hunterAvatar;
    }

    public Integer getHunterLevel() {
        return hunterLevel;
    }

    public void setHunterLevel(Integer hunterLevel) {
        this.hunterLevel = hunterLevel;
    }

    public String getHunterTitle() {
        return hunterTitle;
    }

    public void setHunterTitle(String hunterTitle) {
        this.hunterTitle = hunterTitle;
    }

    public Integer getHunterReputation() {
        return hunterReputation;
    }

    public void setHunterReputation(Integer hunterReputation) {
        this.hunterReputation = hunterReputation;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }

    public String getApplyMessage() {
        return applyMessage;
    }

    public void setApplyMessage(String applyMessage) {
        this.applyMessage = applyMessage;
    }

    public Instant getExpectedFinishTime() {
        return expectedFinishTime;
    }

    public void setExpectedFinishTime(Instant expectedFinishTime) {
        this.expectedFinishTime = expectedFinishTime;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
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
}
