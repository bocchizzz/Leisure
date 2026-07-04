package com.bangboo.marketplace.repository;

import com.bangboo.marketplace.entity.TaskHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    Optional<TaskHistory> findByUserIdAndTaskId(Long userId, Long taskId);

    Page<TaskHistory> findByUserIdOrderByLastViewedAtDesc(Long userId, Pageable pageable);

    @Query("select coalesce(max(h.id), 0) from TaskHistory h")
    long maxId();
}
