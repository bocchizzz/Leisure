package com.bangboo.court.repository;

import com.bangboo.court.entity.CourtVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourtVoteRepository extends JpaRepository<CourtVote, Long> {
    boolean existsByCaseIdAndVoterId(Long caseId, Long voterId);

    List<CourtVote> findByCaseId(Long caseId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CourtVote v where v.caseId = :caseId")
    void deleteByCaseId(Long caseId);
}
