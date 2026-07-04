package com.bangboo.ai.repository;

import com.bangboo.ai.entity.AiCourtSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AiCourtSummaryRepository extends JpaRepository<AiCourtSummary, Long> {
    @Query("select coalesce(max(s.id), 0) from AiCourtSummary s")
    long maxId();
}
