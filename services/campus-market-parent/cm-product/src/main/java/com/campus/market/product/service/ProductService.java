package com.campus.market.product.service;

import com.campus.market.common.exception.BusinessException;
import com.campus.market.common.exception.ResourceNotFoundException;
import com.campus.market.common.response.ApiResponse;
import com.campus.market.product.dto.CreateProductRequest;
import com.campus.market.product.dto.ProductVO;
import com.campus.market.product.dto.UpdateProductRequest;
import com.campus.market.product.entity.Product;
import com.campus.market.product.enums.ProductCategory;
import com.campus.market.product.enums.ProductCondition;
import com.campus.market.product.enums.ProductStatus;
import com.campus.market.product.feign.AuthClient;
import com.campus.market.product.feign.TradeClient;
import com.campus.market.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务
 */
@Service
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    
    @Autowired(required = false)
    private TradeClient tradeClient;
    
    @Autowired(required = false)
    private AuthClient authClient;
    
    @Value("${saga.reserve-timeout-minutes:30}")
    private int reserveTimeoutMinutes;
    
    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * 分页查询商品
     */
    public Page<ProductVO> listProducts(ProductCategory category, ProductCondition condition,
                                        BigDecimal minPrice, BigDecimal maxPrice,
                                        String keyword, Long sellerId, ProductStatus status,
                                        Pageable pageable) {
        return productRepository.findByFilters(category, condition, minPrice, maxPrice, 
                keyword, sellerId, status, pageable)
                .map(product -> {
                    ProductVO vo = ProductVO.fromEntity(product);
                    fillSellerName(vo, product.getSellerId());
                    return vo;
                });
    }
    
    /**
     * 获取商品详情
     */
    public ProductVO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        ProductVO vo = ProductVO.fromEntity(product);
        fillSellerName(vo, product.getSellerId());
        return vo;
    }
    
    /**
     * 填充卖家名称
     */
    private void fillSellerName(ProductVO vo, Long sellerId) {
        if (authClient == null || sellerId == null) return;
        try {
            Map<String, List<Long>> request = new HashMap<>();
            request.put("ids", List.of(sellerId));
            ApiResponse<List<Map<String, Object>>> response = authClient.getUserBriefs(request);
            if (response.getCode() == 200 && response.getData() != null && !response.getData().isEmpty()) {
                Map<String, Object> user = response.getData().get(0);
                vo.setSellerName((String) user.get("username"));
            }
        } catch (Exception e) {
            log.warn("Failed to get seller name for sellerId={}: {}", sellerId, e.getMessage());
        }
    }
    
    /**
     * 获取我的商品列表
     */
    public Page<ProductVO> getMyProducts(Long sellerId, ProductStatus status, Pageable pageable) {
        Page<Product> products = (status != null)
                ? productRepository.findBySellerIdAndStatus(sellerId, status, pageable)
                : productRepository.findBySellerId(sellerId, pageable);
        return products.map(product -> {
            ProductVO vo = ProductVO.fromEntity(product);
            fillSellerName(vo, product.getSellerId());
            return vo;
        });
    }
    
    /**
     * 创建商品
     */
    @Transactional
    public ProductVO createProduct(Long sellerId, CreateProductRequest request) {
        Product product = new Product();
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setCondition(request.getCondition());
        product.setImageUrl(request.getImageUrl());
        product.setSellerId(sellerId);
        product.setStatus(ProductStatus.AVAILABLE);
        
        product = productRepository.save(product);
        log.info("Product created: id={}, title={}, seller={}", product.getId(), product.getTitle(), sellerId);
        
        return ProductVO.fromEntity(product);
    }
    
    /**
     * 更新商品
     */
    @Transactional
    public ProductVO updateProduct(Long productId, Long sellerId, UpdateProductRequest request) {
        Product product = productRepository.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        
        if (product.getStatus() == ProductStatus.SOLD) {
            throw new BusinessException("已售出的商品不能修改");
        }
        
        if (request.getTitle() != null) product.setTitle(request.getTitle());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getCategory() != null) product.setCategory(request.getCategory());
        if (request.getCondition() != null) product.setCondition(request.getCondition());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        
        product = productRepository.save(product);
        return ProductVO.fromEntity(product);
    }
    
    /**
     * 下架商品
     */
    @Transactional
    public void removeProduct(Long productId, Long sellerId) {
        Product product = productRepository.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        
        if (product.getStatus() == ProductStatus.RESERVED) {
            throw new BusinessException("商品已被预订，不能下架");
        }
        
        product.setStatus(ProductStatus.REMOVED);
        productRepository.save(product);
        log.info("Product removed: id={}", productId);
    }
    
    /**
     * 重新上架商品
     */
    @Transactional
    public ProductVO relistProduct(Long productId, Long sellerId) {
        Product product = productRepository.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        
        if (product.getStatus() != ProductStatus.REMOVED) {
            throw new BusinessException("只有已下架的商品才能重新上架");
        }
        
        product.setStatus(ProductStatus.AVAILABLE);
        product = productRepository.save(product);
        return ProductVO.fromEntity(product);
    }
    
    /**
     * 推荐商品
     */
    public List<ProductVO> recommendProducts(int size) {
        return productRepository.findRandomAvailable(PageRequest.of(0, size))
                .stream()
                .map(ProductVO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 价格建议 - 基于已售出商品的历史成交价
     */
    public Map<String, Object> getPriceSuggestion(ProductCategory category, ProductCondition condition) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 首先尝试基于已售商品计算（历史成交价）
        String categoryStr = category.name();
        List<Object[]> soldStatsList = condition != null 
                ? productRepository.findPriceStatsBySoldByCategoryAndCondition(categoryStr, condition.name())
                : productRepository.findPriceStatsBySoldByCategory(categoryStr);
        
        if (soldStatsList != null && !soldStatsList.isEmpty()) {
            Object[] soldStats = soldStatsList.get(0);
            if (soldStats != null && soldStats.length == 3 && soldStats[0] != null) {
                result.put("avgPrice", soldStats[0]);
                result.put("minPrice", soldStats[1]);
                result.put("maxPrice", soldStats[2]);
                result.put("hasData", true);
                result.put("dataSource", "历史成交");
                result.put("suggestion", String.format("建议定价区间: ¥%.0f - ¥%.0f (基于历史成交)", 
                        ((Number)soldStats[1]).doubleValue(), ((Number)soldStats[2]).doubleValue()));
                return result;
            }
        }
        
        // 2. 如果没有已售数据，使用所有商品数据作为参考
        List<Object[]> allStatsList = condition != null
                ? productRepository.findPriceStatsByCategoryAndCondition(categoryStr, condition.name())
                : productRepository.findPriceStatsByCategory(categoryStr);
        
        if (allStatsList != null && !allStatsList.isEmpty()) {
            Object[] allStats = allStatsList.get(0);
            if (allStats != null && allStats.length == 3 && allStats[0] != null) {
                result.put("avgPrice", allStats[0]);
                result.put("minPrice", allStats[1]);
                result.put("maxPrice", allStats[2]);
                result.put("hasData", true);
                result.put("dataSource", "在售参考");
                result.put("suggestion", String.format("参考定价区间: ¥%.0f - ¥%.0f (基于在售商品)", 
                        ((Number)allStats[1]).doubleValue(), ((Number)allStats[2]).doubleValue()));
                return result;
            }
        }
        
        // 3. 无数据
        result.put("avgPrice", null);
        result.put("minPrice", null);
        result.put("maxPrice", null);
        result.put("hasData", false);
        result.put("dataSource", null);
        result.put("suggestion", "暂无同类商品数据，请自行定价");
        
        return result;
    }
    
    // ========== Saga 预占相关 ==========
    
    /**
     * 预占商品（内部接口）
     */
    @Transactional
    public boolean reserveProduct(Long productId, String orderNo) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        
        if (!product.tryReserve(orderNo)) {
            log.warn("Failed to reserve product: id={}, status={}", productId, product.getStatus());
            return false;
        }
        
        productRepository.save(product);
        log.info("Product reserved: id={}, orderNo={}", productId, orderNo);
        return true;
    }
    
    /**
     * 确认预占（内部接口）
     */
    @Transactional
    public boolean confirmReserve(Long productId, String orderNo) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        
        if (!product.confirmReserve(orderNo)) {
            log.warn("Failed to confirm reserve: id={}, orderNo={}", productId, orderNo);
            return false;
        }
        
        productRepository.save(product);
        log.info("Reserve confirmed: id={}, orderNo={}", productId, orderNo);
        return true;
    }
    
    /**
     * 取消预占（内部接口）
     */
    @Transactional
    public boolean cancelReserve(Long productId, String orderNo) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        
        if (!product.cancelReserve(orderNo)) {
            log.warn("Failed to cancel reserve: id={}, orderNo={}", productId, orderNo);
            return false;
        }
        
        productRepository.save(product);
        log.info("Reserve cancelled: id={}, orderNo={}", productId, orderNo);
        return true;
    }
    
    /**
     * 标记已售出（内部接口）
     */
    @Transactional
    public boolean markSold(Long productId, String orderNo) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        
        if (!product.markSold(orderNo)) {
            log.warn("Failed to mark sold: id={}, orderNo={}", productId, orderNo);
            return false;
        }
        
        productRepository.save(product);
        log.info("Product sold: id={}, orderNo={}", productId, orderNo);
        return true;
    }
    
    /**
     * 获取商品简要信息（内部接口，批量）
     */
    public List<Map<String, Object>> getProductBriefs(List<Long> ids) {
        return productRepository.findByIdIn(ids).stream()
                .map(p -> {
                    Map<String, Object> brief = new HashMap<>();
                    brief.put("id", p.getId());
                    brief.put("title", p.getTitle());
                    brief.put("price", p.getPrice());
                    brief.put("imageUrl", p.getImageUrl());
                    brief.put("category", p.getCategory().name());
                    brief.put("status", p.getStatus().name());
                    brief.put("sellerId", p.getSellerId());
                    return brief;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 定时任务：清理悬挂的预占（含反查cm-trade防超卖）
     * 
     * Task T5.6 要求：
     * - 对每条先反查 cm-trade：GET /internal/orders/by-order-no/{orderNo}
     * - 订单存在且非 CANCELLED 且 productId 匹配：补确认，不释放
     * - 订单不存在或已取消：释放 RESERVED -> AVAILABLE
     * - 反查失败/超时：本轮跳过释放 + 告警日志（安全优先，避免超卖）
     */
    @Scheduled(fixedRate = 60000, initialDelay = 60000) // 每分钟执行，启动后60秒开始
    @Transactional
    public void cleanupHangingReservations() {
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(reserveTimeoutMinutes);
        List<Product> hangingProducts = productRepository.findHangingReservations(timeout, PageRequest.of(0, 100));
        
        int released = 0;
        int confirmed = 0;
        int skipped = 0;
        
        for (Product product : hangingProducts) {
            String orderNo = product.getReservedOrderNo();
            log.info("Processing hanging reservation: productId={}, orderNo={}, reservedAt={}",
                    product.getId(), orderNo, product.getReservedAt());
            
            if (orderNo == null || orderNo.isEmpty()) {
                // 无订单号，直接释放
                releaseProduct(product);
                released++;
                continue;
            }
            
            try {
                // 反查 cm-trade 确认订单状态
                ApiResponse<Map<String, Object>> response = tradeClient.getOrderByOrderNo(orderNo);
                
                if (response.getCode() == 200 && response.getData() != null) {
                    Map<String, Object> orderData = response.getData();
                    String status = (String) orderData.get("status");
                    Object productIdObj = orderData.get("productId");
                    Long orderProductId = productIdObj != null ? Long.valueOf(productIdObj.toString()) : null;
                    
                    // 订单存在且非CANCELLED且productId匹配：补确认
                    if (!"CANCELLED".equals(status) && product.getId().equals(orderProductId)) {
                        log.info("Order exists and active, confirming reserve: productId={}, orderNo={}, status={}",
                                product.getId(), orderNo, status);
                        product.setReserveConfirmed(true);
                        productRepository.save(product);
                        confirmed++;
                    } else {
                        // 订单已取消或productId不匹配：释放
                        log.info("Order cancelled or mismatch, releasing: productId={}, orderNo={}, status={}",
                                product.getId(), orderNo, status);
                        releaseProduct(product);
                        released++;
                    }
                } else if (response.getCode() == 404) {
                    // 订单不存在：释放
                    log.info("Order not found, releasing: productId={}, orderNo={}", product.getId(), orderNo);
                    releaseProduct(product);
                    released++;
                } else {
                    // 其他错误：跳过，安全优先
                    log.warn("Unexpected response from trade service, skipping release: productId={}, orderNo={}, code={}",
                            product.getId(), orderNo, response.getCode());
                    skipped++;
                }
            } catch (Exception e) {
                // 反查失败/超时：跳过释放，安全优先避免超卖
                log.error("Failed to query order from trade service, skipping release to prevent overselling: " +
                        "productId={}, orderNo={}, error={}", product.getId(), orderNo, e.getMessage());
                skipped++;
            }
        }
        
        if (!hangingProducts.isEmpty()) {
            log.info("Hanging reservation cleanup completed: released={}, confirmed={}, skipped={}",
                    released, confirmed, skipped);
        }
    }
    
    /**
     * 释放商品预占
     */
    private void releaseProduct(Product product) {
        product.setStatus(ProductStatus.AVAILABLE);
        product.setReservedOrderNo(null);
        product.setReservedAt(null);
        product.setReserveConfirmed(false);
        productRepository.save(product);
        log.info("Product released: id={}", product.getId());
    }
    
    // ========== 管理员接口 ==========
    
    /**
     * 管理员查看所有商品
     */
    public Page<ProductVO> listAllProducts(ProductStatus status, Pageable pageable) {
        return listProducts(null, null, null, null, null, null, status, pageable);
    }
    
    /**
     * 删除商品（管理员）
     */
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        
        product.setStatus(ProductStatus.REMOVED);
        productRepository.save(product);
        log.info("Product deleted by admin: id={}", productId);
    }
    
    /**
     * 商品统计
     */
    public Map<String, Long> getProductStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("available", productRepository.countByStatus(ProductStatus.AVAILABLE));
        stats.put("reserved", productRepository.countByStatus(ProductStatus.RESERVED));
        stats.put("sold", productRepository.countByStatus(ProductStatus.SOLD));
        stats.put("removed", productRepository.countByStatus(ProductStatus.REMOVED));
        return stats;
    }
}
