package com.campus.market.trade.service;

import com.campus.market.common.exception.BusinessException;
import com.campus.market.common.exception.ResourceNotFoundException;
import com.campus.market.common.response.ApiResponse;
import com.campus.market.trade.dto.CreateReviewRequest;
import com.campus.market.trade.dto.ReviewVO;
import com.campus.market.trade.entity.Order;
import com.campus.market.trade.entity.Review;
import com.campus.market.trade.feign.NotifyClient;
import com.campus.market.trade.feign.UserClient;
import com.campus.market.trade.repository.OrderRepository;
import com.campus.market.trade.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评价服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final NotifyClient notifyClient;
    
    /**
     * 创建评价
     */
    @Transactional
    public ReviewVO createReview(Long buyerId, CreateReviewRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));
        
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException("只有买家可以评价");
        }
        if (!order.canReview()) {
            throw new BusinessException("该订单不可评价");
        }
        if (reviewRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new BusinessException("该订单已评价");
        }
        
        Review review = new Review();
        review.setOrderId(order.getId());
        review.setProductId(order.getProductId());
        review.setBuyerId(buyerId);
        review.setSellerId(order.getSellerId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        
        review = reviewRepository.save(review);
        
        // 更新订单评价状态
        order.setReviewed(true);
        orderRepository.save(order);
        
        // 根据评分更新卖家信誉
        int reputationDelta = calculateReputationDelta(request.getRating());
        try {
            userClient.updateReputation(order.getSellerId(), reputationDelta);
        } catch (Exception e) {
            log.warn("Failed to update seller reputation: {}", e.getMessage());
        }
        
        // 发送评价通知
        sendReviewNotification(order.getSellerId(), review, order.getProductTitle());
        
        log.info("Review created: orderId={}, rating={}", order.getId(), request.getRating());
        return enrichReviewVO(ReviewVO.fromEntity(review));
    }
    
    /**
     * 获取卖家评价
     */
    public Page<ReviewVO> getSellerReviews(Long sellerId, Pageable pageable) {
        return reviewRepository.findBySellerId(sellerId, pageable)
                .map(review -> enrichReviewVO(ReviewVO.fromEntity(review)));
    }
    
    /**
     * 获取商品评价
     */
    public Page<ReviewVO> getProductReviews(Long productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable)
                .map(review -> enrichReviewVO(ReviewVO.fromEntity(review)));
    }
    
    /**
     * 获取订单评价
     * 找不到评价时返回null（而非抛404），兼容前端期望
     */
    public ReviewVO getOrderReview(Long orderId) {
        return reviewRepository.findByOrderId(orderId)
                .map(review -> enrichReviewVO(ReviewVO.fromEntity(review)))
                .orElse(null);
    }
    
    /**
     * 获取卖家平均评分
     */
    public Double getSellerAverageRating(Long sellerId) {
        return reviewRepository.getAverageRatingBySellerId(sellerId);
    }
    
    // ========== 私有方法 ==========
    
    private int calculateReputationDelta(int rating) {
        return switch (rating) {
            case 5 -> 5;
            case 4 -> 2;
            case 3 -> 0;
            case 2 -> -3;
            case 1 -> -5;
            default -> 0;
        };
    }
    
    private ReviewVO enrichReviewVO(ReviewVO vo) {
        try {
            Map<String, List<Long>> req = new HashMap<>();
            req.put("ids", Collections.singletonList(vo.getBuyerId()));
            
            ApiResponse<List<Map<String, Object>>> resp = userClient.getUserBriefs(req);
            if (resp.getCode() == 200 && resp.getData() != null && !resp.getData().isEmpty()) {
                vo.setBuyerName((String) resp.getData().get(0).get("username"));
            }
        } catch (Exception e) {
            log.warn("Failed to enrich review with buyer name: {}", e.getMessage());
        }
        return vo;
    }
    
    private void sendReviewNotification(Long sellerId, Review review, String productTitle) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("userId", sellerId);
            message.put("type", "REVIEW");
            message.put("title", "收到新评价");
            message.put("content", "你的商品「" + productTitle + "」收到了" + review.getRating() + "星评价");
            message.put("relatedId", review.getOrderId());
            notifyClient.sendMessage(message);
        } catch (Exception e) {
            log.warn("Failed to send review notification: {}", e.getMessage());
        }
    }
}
