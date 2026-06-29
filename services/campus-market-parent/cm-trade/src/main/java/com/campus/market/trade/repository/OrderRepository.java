package com.campus.market.trade.repository;

import com.campus.market.trade.entity.Order;
import com.campus.market.trade.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 订单仓库
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderNo(String orderNo);
    
    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);
    
    Page<Order> findBySellerId(Long sellerId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.buyerId = :buyerId AND (:status IS NULL OR o.status = :status)")
    Page<Order> findByBuyerIdAndStatus(@Param("buyerId") Long buyerId, 
                                        @Param("status") OrderStatus status, 
                                        Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.sellerId = :sellerId AND (:status IS NULL OR o.status = :status)")
    Page<Order> findBySellerIdAndStatus(@Param("sellerId") Long sellerId, 
                                         @Param("status") OrderStatus status, 
                                         Pageable pageable);
    
    long countByStatus(OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.buyerId = :userId OR o.sellerId = :userId")
    long countByUserId(@Param("userId") Long userId);
}
