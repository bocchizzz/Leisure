package com.bangboo.court.entity;

import com.bangboo.court.enums.RulingResult;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "court_ruling",
        uniqueConstraints = @UniqueConstraint(name = "uk_court_ruling_case", columnNames = "case_id"),
        indexes = @Index(name = "idx_court_ruling_created", columnList = "created_at")
)
public class CourtRuling {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "admin_name", length = 80)
    private String adminName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RulingResult result;

    @Column(name = "bounty_release_rate", nullable = false)
    private double bountyReleaseRate;

    @Column(name = "publisher_credit_delta", nullable = false)
    private int publisherCreditDelta;

    @Column(name = "hunter_credit_delta", nullable = false)
    private int hunterCreditDelta;

    @Column(nullable = false, length = 2000)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public RulingResult getResult() {
        return result;
    }

    public void setResult(RulingResult result) {
        this.result = result;
    }

    public double getBountyReleaseRate() {
        return bountyReleaseRate;
    }

    public void setBountyReleaseRate(double bountyReleaseRate) {
        this.bountyReleaseRate = bountyReleaseRate;
    }

    public int getPublisherCreditDelta() {
        return publisherCreditDelta;
    }

    public void setPublisherCreditDelta(int publisherCreditDelta) {
        this.publisherCreditDelta = publisherCreditDelta;
    }

    public int getHunterCreditDelta() {
        return hunterCreditDelta;
    }

    public void setHunterCreditDelta(int hunterCreditDelta) {
        this.hunterCreditDelta = hunterCreditDelta;
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
}
