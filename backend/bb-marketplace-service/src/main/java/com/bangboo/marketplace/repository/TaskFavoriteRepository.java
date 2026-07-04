package com.bangboo.marketplace.repository;

import com.bangboo.marketplace.entity.TaskFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TaskFavoriteRepository extends JpaRepository<TaskFavorite, Long> {
    Optional<TaskFavorite> findByUserIdAndTaskId(Long userId, Long taskId);

    boolean existsByUserIdAndTaskId(Long userId, Long taskId);

    Page<TaskFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    void deleteByUserIdAndTaskId(Long userId, Long taskId);

    @Query("select coalesce(max(f.id), 0) from TaskFavorite f")
    long maxId();
}
