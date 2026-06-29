package com.campus.market.product.repository;

import com.campus.market.product.entity.Product;
import com.campus.market.product.enums.ProductCategory;
import com.campus.market.product.enums.ProductCondition;
import com.campus.market.product.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 商品仓库
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR p.category = :category) " +
           "AND (:condition IS NULL OR p.condition = :condition) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.description LIKE %:keyword%) " +
           "AND (:sellerId IS NULL OR p.sellerId = :sellerId) " +
           "AND ((:status IS NULL AND p.status != 'REMOVED') OR p.status = :status) " +
           "ORDER BY CASE p.status WHEN 'AVAILABLE' THEN 1 WHEN 'RESERVED' THEN 2 WHEN 'SOLD' THEN 3 ELSE 4 END, p.createdAt DESC")
    Page<Product> findByFilters(
            @Param("category") ProductCategory category,
            @Param("condition") ProductCondition condition,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("keyword") String keyword,
            @Param("sellerId") Long sellerId,
            @Param("status") ProductStatus status,
            Pageable pageable);
    
    Page<Product> findBySellerId(Long sellerId, Pageable pageable);
    
    Page<Product> findBySellerIdAndStatus(Long sellerId, ProductStatus status, Pageable pageable);
    
    List<Product> findByIdIn(List<Long> ids);
    
    @Query(value = "SELECT * FROM biz_product WHERE status = 'AVAILABLE' ORDER BY RAND()", nativeQuery = true)
    List<Product> findRandomAvailable(Pageable pageable);
    
    @Query(value = "SELECT AVG(price) as avgPrice, MIN(price) as minPrice, MAX(price) as maxPrice FROM biz_product WHERE category = :category AND status = 'SOLD'", nativeQuery = true)
    List<Object[]> findPriceStatsBySoldByCategory(@Param("category") String category);
    
    @Query(value = "SELECT AVG(price) as avgPrice, MIN(price) as minPrice, MAX(price) as maxPrice FROM biz_product WHERE category = :category AND biz_product.condition = :condition AND status = 'SOLD'", nativeQuery = true)
    List<Object[]> findPriceStatsBySoldByCategoryAndCondition(@Param("category") String category, @Param("condition") String condition);
    
    @Query(value = "SELECT AVG(price) as avgPrice, MIN(price) as minPrice, MAX(price) as maxPrice FROM biz_product WHERE category = :category AND status IN ('AVAILABLE', 'RESERVED', 'SOLD')", nativeQuery = true)
    List<Object[]> findPriceStatsByCategory(@Param("category") String category);
    
    @Query(value = "SELECT AVG(price) as avgPrice, MIN(price) as minPrice, MAX(price) as maxPrice FROM biz_product WHERE category = :category AND biz_product.condition = :condition AND status IN ('AVAILABLE', 'RESERVED', 'SOLD')", nativeQuery = true)
    List<Object[]> findPriceStatsByCategoryAndCondition(@Param("category") String category, @Param("condition") String condition);
    
    // Saga相关：查找未确认的悬挂预占
    @Query("SELECT p FROM Product p WHERE p.status = 'RESERVED' AND p.reserveConfirmed = false AND p.reservedAt < :timeout")
    List<Product> findHangingReservations(@Param("timeout") LocalDateTime timeout, Pageable pageable);
    
    Optional<Product> findByIdAndSellerId(Long id, Long sellerId);
    
    long countByStatus(ProductStatus status);
}
