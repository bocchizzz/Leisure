package com.bangboo.marketplace.repository;

import com.bangboo.marketplace.entity.Task;
import com.bangboo.marketplace.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    Page<Task> findByPublisherId(Long publisherId, Pageable pageable);

    Page<Task> findByAssignedHunterId(Long assignedHunterId, Pageable pageable);

    List<Task> findByStatusOrderByCreatedAtDesc(TaskStatus status, Pageable pageable);

    long countByStatus(TaskStatus status);

    @Query("select coalesce(max(t.id), 0) from Task t")
    long maxId();
}
