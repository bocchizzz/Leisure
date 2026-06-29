package com.campus.market.notify.repository;

import com.campus.market.notify.entity.Appeal;
import com.campus.market.notify.enums.AppealStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 申诉仓库
 */
@Repository
public interface AppealRepository extends JpaRepository<Appeal, Long> {
    
    Page<Appeal> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Page<Appeal> findByStatusOrderByCreatedAtDesc(AppealStatus status, Pageable pageable);
    
    Page<Appeal> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Optional<Appeal> findByIdAndUserId(Long id, Long userId);
    
    long countByStatus(AppealStatus status);
}
