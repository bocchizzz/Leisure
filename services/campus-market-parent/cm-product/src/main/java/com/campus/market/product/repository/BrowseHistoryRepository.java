package com.campus.market.product.repository;

import com.campus.market.product.entity.BrowseHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 浏览历史仓库
 */
@Repository
public interface BrowseHistoryRepository extends JpaRepository<BrowseHistory, Long> {
    
    Page<BrowseHistory> findByUserIdOrderByViewedAtDesc(Long userId, Pageable pageable);
    
    @Modifying
    @Query("DELETE FROM BrowseHistory h WHERE h.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("DELETE FROM BrowseHistory h WHERE h.userId = :userId AND h.productId = :productId")
    void deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
}
