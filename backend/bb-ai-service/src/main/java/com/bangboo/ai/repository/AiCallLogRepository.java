package com.bangboo.ai.repository;

import com.bangboo.ai.entity.AiCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AiCallLogRepository extends JpaRepository<AiCallLog, Long> {
    @Query("select coalesce(max(l.id), 0) from AiCallLog l")
    long maxId();
}
