package com.campus.market.trade.dto;

import com.campus.market.trade.entity.Review;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价视图对象
 */
@Data
public class ReviewVO {
    
    private Long id;
    private Long orderId;
    private Long productId;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    
    public static ReviewVO fromEntity(Review review) {
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setOrderId(review.getOrderId());
        vo.setProductId(review.getProductId());
        vo.setBuyerId(review.getBuyerId());
        vo.setSellerId(review.getSellerId());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setCreatedAt(review.getCreatedAt());
        return vo;
    }
}
