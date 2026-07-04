package com.bangboo.ai.repository;

import com.bangboo.ai.entity.AiTaskSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AiTaskSuggestionRepository extends JpaRepository<AiTaskSuggestion, Long> {
    @Query("select coalesce(max(s.id), 0) from AiTaskSuggestion s")
    long maxId();
}
