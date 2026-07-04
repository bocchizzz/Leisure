package com.bangboo.reputation.repository;

import com.bangboo.reputation.entity.CreditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CreditLogRepository extends JpaRepository<CreditLog, Long> {
    Page<CreditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("select coalesce(max(c.id), 0) from CreditLog c")
    long maxId();
}
