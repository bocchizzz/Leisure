package com.bangboo.court.entity;

import com.bangboo.court.enums.CourtVoteOption;
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
        name = "court_vote",
        uniqueConstraints = @UniqueConstraint(name = "uk_court_vote_case_voter", columnNames = {"case_id", "voter_id"}),
        indexes = {
                @Index(name = "idx_court_vote_case", columnList = "case_id"),
                @Index(name = "idx_court_vote_voter", columnList = "voter_id")
        }
)
public class CourtVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "voter_id", nullable = false)
    private Long voterId;

    @Column(name = "voter_name", length = 80)
    private String voterName;

    @Enumerated(EnumType.STRING)
    @Column(name = \u0022vote_option\u0022, nullable = false, length = 40)
    private CourtVoteOption option;

    @Column(nullable = false)
    private int weight = 1;

    @Column(length = 600)
    private String reason;

    @Column(name = "is_adopted")
    private Boolean adopted = false;

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

    public Long getVoterId() {
        return voterId;
    }

    public void setVoterId(Long voterId) {
        this.voterId = voterId;
    }

    public String getVoterName() {
        return voterName;
    }

    public void setVoterName(String voterName) {
        this.voterName = voterName;
    }

    public CourtVoteOption getOption() {
        return option;
    }

    public void setOption(CourtVoteOption option) {
        this.option = option;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getAdopted() {
        return adopted;
    }

    public void setAdopted(Boolean adopted) {
        this.adopted = adopted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
