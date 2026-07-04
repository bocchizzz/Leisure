package com.bangboo.court.repository;

import com.bangboo.court.entity.CourtRuling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CourtRulingRepository extends JpaRepository<CourtRuling, Long> {
    Optional<CourtRuling> findByCaseId(Long caseId);

    boolean existsByCaseId(Long caseId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CourtRuling r where r.caseId = :caseId")
    void deleteByCaseId(Long caseId);
}
