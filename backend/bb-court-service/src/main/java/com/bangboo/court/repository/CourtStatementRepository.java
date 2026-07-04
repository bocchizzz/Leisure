package com.bangboo.court.repository;

import com.bangboo.court.entity.CourtStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourtStatementRepository extends JpaRepository<CourtStatement, Long> {
    List<CourtStatement> findByCaseIdOrderByCreatedAtAsc(Long caseId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CourtStatement s where s.caseId = :caseId")
    void deleteByCaseId(Long caseId);
}
