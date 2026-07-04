package com.bangboo.court.repository;

import com.bangboo.court.entity.CourtCase;
import com.bangboo.court.enums.CourtCaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourtCaseRepository extends JpaRepository<CourtCase, Long> {
    boolean existsByContractIdAndStatusNot(Long contractId, CourtCaseStatus status);

    Optional<CourtCase> findByCaseNo(String caseNo);

    Page<CourtCase> findByStatus(CourtCaseStatus status, Pageable pageable);

    List<CourtCase> findByStatus(CourtCaseStatus status);

    @Query("select coalesce(max(c.id), 0) from CourtCase c")
    Long maxId();
}
