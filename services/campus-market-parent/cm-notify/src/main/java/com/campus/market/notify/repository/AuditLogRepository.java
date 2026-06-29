package com.campus.market.notify.repository;

import com.campus.market.notify.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 审计日志仓库
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:action IS NULL OR a.action LIKE %:action%) " +
           "AND (:username IS NULL OR a.username LIKE %:username%) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> findByFilters(@Param("action") String action, 
                                  @Param("username") String username, 
                                  Pageable pageable);
}
