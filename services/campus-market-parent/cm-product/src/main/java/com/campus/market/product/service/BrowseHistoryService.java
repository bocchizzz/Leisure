package com.campus.market.product.service;

import com.campus.market.product.dto.ProductVO;
import com.campus.market.product.entity.BrowseHistory;
import com.campus.market.product.entity.Product;
import com.campus.market.product.repository.BrowseHistoryRepository;
import com.campus.market.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 浏览历史服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BrowseHistoryService {
    
    private final BrowseHistoryRepository browseHistoryRepository;
    private final ProductRepository productRepository;
    
    /**
     * 浏览历史列表
     */
    public Page<ProductVO> listHistory(Long userId, Pageable pageable) {
        Page<BrowseHistory> historyPage = browseHistoryRepository.findByUserIdOrderByViewedAtDesc(userId, pageable);
        
        List<Long> productIds = historyPage.getContent().stream()
                .map(BrowseHistory::getProductId)
                .collect(Collectors.toList());
        
        Map<Long, Product> productMap = productRepository.findByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        List<ProductVO> productVOs = historyPage.getContent().stream()
                .map(h -> productMap.get(h.getProductId()))
                .filter(p -> p != null)
                .map(ProductVO::fromEntity)
                .collect(Collectors.toList());
        
        return new PageImpl<>(productVOs, pageable, historyPage.getTotalElements());
    }
    
    /**
     * 记录浏览
     */
    @Transactional
    public void recordView(Long userId, Long productId) {
        // 先删除旧记录
        browseHistoryRepository.deleteByUserIdAndProductId(userId, productId);
        
        // 添加新记录
        BrowseHistory history = new BrowseHistory();
        history.setUserId(userId);
        history.setProductId(productId);
        browseHistoryRepository.save(history);
    }
    
    /**
     * 清空浏览历史
     */
    @Transactional
    public void clearHistory(Long userId) {
        browseHistoryRepository.deleteByUserId(userId);
        log.info("Browse history cleared: userId={}", userId);
    }
}
