package com.campus.market.product.service;

import com.campus.market.common.exception.BusinessException;
import com.campus.market.common.exception.ResourceNotFoundException;
import com.campus.market.product.dto.ProductVO;
import com.campus.market.product.entity.Favorite;
import com.campus.market.product.entity.Product;
import com.campus.market.product.repository.FavoriteRepository;
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
 * 收藏服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {
    
    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    
    /**
     * 收藏列表
     */
    public Page<ProductVO> listFavorites(Long userId, Pageable pageable) {
        Page<Favorite> favoritePage = favoriteRepository.findByUserId(userId, pageable);
        
        List<Long> productIds = favoritePage.getContent().stream()
                .map(Favorite::getProductId)
                .collect(Collectors.toList());
        
        Map<Long, Product> productMap = productRepository.findByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        List<ProductVO> productVOs = favoritePage.getContent().stream()
                .map(f -> productMap.get(f.getProductId()))
                .filter(p -> p != null)
                .map(p -> {
                    ProductVO vo = ProductVO.fromEntity(p);
                    vo.setIsFavorited(true);
                    return vo;
                })
                .collect(Collectors.toList());
        
        return new PageImpl<>(productVOs, pageable, favoritePage.getTotalElements());
    }
    
    /**
     * 添加收藏
     */
    @Transactional
    public void addFavorite(Long userId, Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", productId);
        }
        
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BusinessException("已收藏该商品");
        }
        
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favoriteRepository.save(favorite);
        
        log.info("Favorite added: userId={}, productId={}", userId, productId);
    }
    
    /**
     * 取消收藏
     */
    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
        log.info("Favorite removed: userId={}, productId={}", userId, productId);
    }
    
    /**
     * 检查是否已收藏
     */
    public boolean isFavorite(Long userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }
}
