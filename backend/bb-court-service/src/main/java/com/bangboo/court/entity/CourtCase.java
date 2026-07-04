package com.bangboo.court.entity;

import com.bangboo.court.enums.CourtCaseStatus;
import com.bangboo.court.enums.CourtCaseType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "court_case",
        indexes = {
                @Index(name = "idx_court_case_contract", columnList = "contract_id"),
                @Index(name = "idx_court_case_status", columnList = "status"),
                @Index(name = "idx_court_case_created", columnList = "created_at")
        }
)
public class CourtCase {
    @Id
    private Long id;

    @Column(name = "case_no", nullable = false, length = 40, unique = true)
    private String caseNo;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "task_title", length = 200)
    private String taskTitle;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "case_title", nullable = false, length = 200)
    private String caseTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CourtCaseType type = CourtCaseType.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CourtCaseStatus status = CourtCaseStatus.VOTING;

    @Column(name = "plaintiff_id", nullable = false)
    private Long plaintiffId;

    @Column(name = "plaintiff_name", length = 80)
    private String plaintiffName;

    @Column(name = "defendant_id", nullable = false)
    private Long defendantId;

    @Column(name = "defendant_name", length = 80)
    private String defendantName;

    @Column(name = "initial_statement", length = 2000)
    private String initialStatement;

    @Column(length = 2000)
    private String summary;

    @Column(name = "ai_summary", length = 2000)
    private String aiSummary;

    @Column(name = "ai_evidence_analysis", length = 2000)
    private String aiEvidenceAnalysis;

    @Column(name = "ai_roast", length = 2000)
    private String aiRoast;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "ruled_at")
    private Instant ruledAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (caseNo == null || caseNo.isBlank()) {
            caseNo = "COURT-" + now.toEpochMilli();
        }
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

    public String getCaseNo() {
        return caseNo;
    }

    public void setCaseNo(String caseNo) {
        this.caseNo = caseNo;
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

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public String getCaseTitle() {
        return caseTitle;
    }

    public void setCaseTitle(String caseTitle) {
        this.caseTitle = caseTitle;
    }

    public CourtCaseType getType() {
        return type;
    }

    public void setType(CourtCaseType type) {
        this.type = type;
    }

    public CourtCaseStatus getStatus() {
        return status;
    }

    public void setStatus(CourtCaseStatus status) {
        this.status = status;
    }

    public Long getPlaintiffId() {
        return plaintiffId;
    }

    public void setPlaintiffId(Long plaintiffId) {
        this.plaintiffId = plaintiffId;
    }

    public String getPlaintiffName() {
        return plaintiffName;
    }

    public void setPlaintiffName(String plaintiffName) {
        this.plaintiffName = plaintiffName;
    }

    public Long getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(Long defendantId) {
        this.defendantId = defendantId;
    }

    public String getDefendantName() {
        return defendantName;
    }

    public void setDefendantName(String defendantName) {
        this.defendantName = defendantName;
    }

    public String getInitialStatement() {
        return initialStatement;
    }

    public void setInitialStatement(String initialStatement) {
        this.initialStatement = initialStatement;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }

    public String getAiEvidenceAnalysis() {
        return aiEvidenceAnalysis;
    }

    public void setAiEvidenceAnalysis(String aiEvidenceAnalysis) {
        this.aiEvidenceAnalysis = aiEvidenceAnalysis;
    }

    public String getAiRoast() {
        return aiRoast;
    }

    public void setAiRoast(String aiRoast) {
        this.aiRoast = aiRoast;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getRuledAt() {
        return ruledAt;
    }

    public void setRuledAt(Instant ruledAt) {
        this.ruledAt = ruledAt;
    }
}
