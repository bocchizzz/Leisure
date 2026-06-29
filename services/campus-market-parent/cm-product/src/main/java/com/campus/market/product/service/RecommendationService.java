package com.campus.market.product.service;

import com.campus.market.product.dto.ProductVO;
import com.campus.market.product.entity.BrowseHistory;
import com.campus.market.product.entity.Product;
import com.campus.market.product.enums.ProductCategory;
import com.campus.market.product.enums.ProductStatus;
import com.campus.market.product.repository.BrowseHistoryRepository;
import com.campus.market.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能推荐服务
 * 基于用户浏览历史进行个性化推荐
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final ProductRepository productRepository;
    private final BrowseHistoryRepository browseHistoryRepository;

    /**
     * 获取个性化推荐商品
     * 算法：
     * 1. 分析用户浏览历史中的商品类别偏好
     * 2. 优先推荐用户偏好类别中未浏览过的商品
     * 3. 不足时补充热门商品
     * 4. 排除用户自己发布的商品
     */
    public List<ProductVO> getPersonalizedRecommendations(Long userId, int limit) {
        List<ProductVO> recommendations = new ArrayList<>();
        Set<Long> excludeProductIds = new HashSet<>();
        
        if (userId != null) {
            // 1. 获取用户浏览历史
            List<BrowseHistory> history = browseHistoryRepository
                    .findByUserIdOrderByViewedAtDesc(userId, PageRequest.of(0, 50))
                    .getContent();
            
            if (!history.isEmpty()) {
                // 获取浏览过的商品ID
                List<Long> viewedProductIds = history.stream()
                        .map(BrowseHistory::getProductId)
                        .collect(Collectors.toList());
                excludeProductIds.addAll(viewedProductIds);
                
                // 2. 分析类别偏好（按浏览次数加权）
                Map<ProductCategory, Long> categoryPreference = new HashMap<>();
                List<Product> viewedProducts = productRepository.findByIdIn(viewedProductIds);
                
                for (Product p : viewedProducts) {
                    categoryPreference.merge(p.getCategory(), 1L, Long::sum);
                }
                
                // 3. 按偏好排序类别
                List<ProductCategory> preferredCategories = categoryPreference.entrySet().stream()
                        .sorted(Map.Entry.<ProductCategory, Long>comparingByValue().reversed())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
                
                // 4. 从偏好类别中推荐未浏览过的商品
                for (ProductCategory category : preferredCategories) {
                    if (recommendations.size() >= limit) break;
                    
                    List<Product> categoryProducts = productRepository.findByFilters(
                            category, null, null, null, null, null, ProductStatus.AVAILABLE,
                            PageRequest.of(0, limit)
                    ).getContent();
                    
                    for (Product p : categoryProducts) {
                        if (recommendations.size() >= limit) break;
                        // 排除已浏览、已推荐、自己发布的商品
                        if (!excludeProductIds.contains(p.getId()) && !userId.equals(p.getSellerId())) {
                            recommendations.add(ProductVO.fromEntity(p));
                            excludeProductIds.add(p.getId());
                        }
                    }
                }
            }
        }
        
        // 5. 不足时补充随机热门商品（排除自己发布的和已推荐的）
        if (recommendations.size() < limit) {
            int remaining = limit - recommendations.size();
            List<Product> randomProducts = productRepository.findRandomAvailable(PageRequest.of(0, remaining + 10));
            
            // 记录已推荐的商品ID（不包括浏览历史）
            Set<Long> recommendedIds = recommendations.stream()
                    .map(ProductVO::getId)
                    .collect(Collectors.toSet());
            
            for (Product p : randomProducts) {
                if (recommendations.size() >= limit) break;
                // 排除自己发布的和已推荐的，但允许已浏览过的
                if (!recommendedIds.contains(p.getId()) && (userId == null || !userId.equals(p.getSellerId()))) {
                    recommendations.add(ProductVO.fromEntity(p));
                    recommendedIds.add(p.getId());
                }
            }
        }
        
        log.debug("Generated {} recommendations for user {}", recommendations.size(), userId);
        return recommendations;
    }
}
