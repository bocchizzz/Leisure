package com.campus.market.notify.repository;

import com.campus.market.notify.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 消息仓库
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    Page<Message> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Page<Message> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.userId = :userId AND m.isRead = false")
    int countUnreadByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.userId = :userId")
    void markAllAsRead(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.id = :id AND m.userId = :userId")
    void markAsRead(@Param("id") Long id, @Param("userId") Long userId);
}
