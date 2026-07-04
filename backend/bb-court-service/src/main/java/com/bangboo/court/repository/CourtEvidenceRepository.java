package com.bangboo.court.repository;

import com.bangboo.court.entity.CourtEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourtEvidenceRepository extends JpaRepository<CourtEvidence, Long> {
    List<CourtEvidence> findByCaseIdOrderByCreatedAtDesc(Long caseId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CourtEvidence e where e.caseId = :caseId")
    void deleteByCaseId(Long caseId);
}
