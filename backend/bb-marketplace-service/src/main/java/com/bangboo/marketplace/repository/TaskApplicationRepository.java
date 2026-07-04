package com.bangboo.marketplace.repository;

import com.bangboo.marketplace.entity.TaskApplication;
import com.bangboo.marketplace.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TaskApplicationRepository extends JpaRepository<TaskApplication, Long> {
    List<TaskApplication> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    List<TaskApplication> findByTaskIdAndStatus(Long taskId, ApplicationStatus status);

    Optional<TaskApplication> findByTaskIdAndHunterIdAndStatus(Long taskId, Long hunterId, ApplicationStatus status);

    Page<TaskApplication> findByHunterId(Long hunterId, Pageable pageable);

    Page<TaskApplication> findByHunterIdAndStatus(Long hunterId, ApplicationStatus status, Pageable pageable);

    long countByTaskId(Long taskId);

    default int countByTaskIdIntValue(Long taskId) {
        return (int) countByTaskId(taskId);
    }

    @Query("select coalesce(max(a.id), 0) from TaskApplication a")
    long maxId();
}
