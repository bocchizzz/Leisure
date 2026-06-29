package com.campus.market.trade.repository;

import com.campus.market.trade.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 评价仓库
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Optional<Review> findByOrderId(Long orderId);
    
    Page<Review> findBySellerId(Long sellerId, Pageable pageable);
    
    Page<Review> findByProductId(Long productId, Pageable pageable);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.sellerId = :sellerId")
    Double getAverageRatingBySellerId(@Param("sellerId") Long sellerId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.sellerId = :sellerId")
    long countBySellerId(@Param("sellerId") Long sellerId);
}
