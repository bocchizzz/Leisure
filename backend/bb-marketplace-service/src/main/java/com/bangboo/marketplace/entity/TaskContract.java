package com.bangboo.marketplace.entity;

import com.bangboo.marketplace.enums.BountyType;
import com.bangboo.marketplace.enums.ContractStatus;
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
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(
        name = "mkt_task_contract",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mkt_contract_task", columnNames = {"task_id"})
        },
        indexes = {
                @Index(name = "idx_mkt_contract_publisher", columnList = "publisher_id,status"),
                @Index(name = "idx_mkt_contract_hunter", columnList = "hunter_id,status"),
                @Index(name = "idx_mkt_contract_status", columnList = "status")
        }
)
public class TaskContract {
    @Id
    private Long id;

    @Column(name = "contract_no", length = 50)
    private String contractNo;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "task_title", length = 100)
    private String taskTitle;

    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "publisher_id", nullable = false)
    private Long publisherId;

    @Column(name = "publisher_name", length = 80)
    private String publisherName;

    @Column(name = "publisher_avatar", length = 500)
    private String publisherAvatar;

    @Column(name = "hunter_id", nullable = false)
    private Long hunterId;

    @Column(name = "hunter_name", length = 80)
    private String hunterName;

    @Column(name = "hunter_avatar", length = 500)
    private String hunterAvatar;

    @Column(name = "bounty_amount", nullable = false)
    private Double bountyAmount = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "bounty_type", nullable = false, length = 20)
    private BountyType bountyType = BountyType.POINT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ContractStatus status = ContractStatus.IN_PROGRESS;

    @Column(name = "completion_standard", length = 1000)
    private String completionStandard;

    @Column(name = "evidence_requirement", length = 1000)
    private String evidenceRequirement;

    @Column(name = "reviewed_by_publisher", nullable = false)
    private Boolean reviewedByPublisher = false;

    @Column(name = "reviewed_by_hunter", nullable = false)
    private Boolean reviewedByHunter = false;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

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

    public String getContractNo() {
        return contractNo;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
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

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
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

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
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

    public Boolean getReviewedByPublisher() {
        return reviewedByPublisher;
    }

    public void setReviewedByPublisher(Boolean reviewedByPublisher) {
        this.reviewedByPublisher = reviewedByPublisher;
    }

    public Boolean getReviewedByHunter() {
        return reviewedByHunter;
    }

    public void setReviewedByHunter(Boolean reviewedByHunter) {
        this.reviewedByHunter = reviewedByHunter;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
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
