package com.bangboo.court.repository;

import com.bangboo.court.entity.CourtPrecedent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CourtPrecedentRepository extends JpaRepository<CourtPrecedent, Long> {
    Page<CourtPrecedent> findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(String title, String summary, Pageable pageable);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CourtPrecedent p where p.caseId = :caseId")
    void deleteByCaseId(Long caseId);
}
