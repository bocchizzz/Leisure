package com.campus.market.trade.service;

import com.campus.market.common.exception.BusinessException;
import com.campus.market.common.exception.ResourceNotFoundException;
import com.campus.market.common.response.ApiResponse;
import com.campus.market.trade.dto.CreateOrderRequest;
import com.campus.market.trade.dto.OrderVO;
import com.campus.market.trade.entity.Order;
import com.campus.market.trade.enums.OrderStatus;
import com.campus.market.trade.feign.NotifyClient;
import com.campus.market.trade.feign.ProductClient;
import com.campus.market.trade.feign.UserClient;
import com.campus.market.trade.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 订单服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final NotifyClient notifyClient;
    
    private static final AtomicLong orderSeq = new AtomicLong(System.currentTimeMillis() % 10000);
    
    /**
     * 创建订单（含Saga预占）
     */
    @Transactional
    public OrderVO createOrder(Long buyerId, CreateOrderRequest request) {
        Long productId = request.getProductId();
        
        // 1. 获取商品信息
        ApiResponse<Map<String, Object>> productResp = productClient.getProduct(productId);
        if (productResp.getCode() != 200 || productResp.getData() == null) {
            throw new BusinessException("商品不存在或已下架");
        }
        
        Map<String, Object> product = productResp.getData();
        Long sellerId = Long.valueOf(product.get("sellerId").toString());
        String status = product.get("status").toString();
        
        // 2. 校验
        if (!"AVAILABLE".equals(status)) {
            throw new BusinessException("商品不可购买");
        }
        if (sellerId.equals(buyerId)) {
            throw new BusinessException("不能购买自己的商品");
        }
        
        // 3. 检查买家状态
        try {
            ApiResponse<Map<String, Object>> userStatus = userClient.getUserStatus(buyerId);
            if (userStatus.getData() != null && "BANNED".equals(userStatus.getData().get("status"))) {
                throw new BusinessException("账号已被封禁，无法下单");
            }
        } catch (Exception e) {
            log.warn("Failed to check user status: {}", e.getMessage());
        }
        
        // 4. 生成订单号
        String orderNo = generateOrderNo();
        
        // 5. Saga第一步：预占商品
        ApiResponse<Boolean> reserveResp = productClient.reserveProduct(productId, orderNo);
        if (reserveResp.getCode() != 200 || !Boolean.TRUE.equals(reserveResp.getData())) {
            throw new BusinessException("商品预占失败，请稍后重试");
        }
        
        try {
            // 6. 创建订单
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setProductId(productId);
            order.setBuyerId(buyerId);
            order.setSellerId(sellerId);
            order.setTotalPrice(new BigDecimal(product.get("price").toString()));
            order.setStatus(OrderStatus.PENDING);
            order.setProductTitle((String) product.get("title"));
            order.setProductImage((String) product.get("imageUrl"));
            order.setProductCategory((String) product.get("category"));
            
            order = orderRepository.save(order);
            log.info("Order created: orderNo={}, productId={}, buyer={}, seller={}", 
                    orderNo, productId, buyerId, sellerId);
            
            // 7. 发送通知给卖家
            sendOrderNotification(sellerId, order, "新订单待确认", 
                    "你的商品「" + order.getProductTitle() + "」有新订单，快去确认吧～");
            
            return OrderVO.fromEntity(order);
            
        } catch (Exception e) {
            // 8. Saga补偿：取消预占
            log.error("Order creation failed, compensating: {}", e.getMessage());
            try {
                productClient.cancelReserve(productId, orderNo);
            } catch (Exception ex) {
                log.error("Failed to compensate reserve: {}", ex.getMessage());
            }
            throw new BusinessException("订单创建失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取订单详情
     */
    public OrderVO getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权查看此订单");
        }
        
        return enrichOrderVO(OrderVO.fromEntity(order));
    }
    
    /**
     * 买家订单列表
     */
    public Page<OrderVO> getBuyerOrders(Long buyerId, OrderStatus status, Pageable pageable) {
        return orderRepository.findByBuyerIdAndStatus(buyerId, status, pageable)
                .map(order -> enrichOrderVO(OrderVO.fromEntity(order)));
    }
    
    /**
     * 卖家订单列表
     */
    public Page<OrderVO> getSellerOrders(Long sellerId, OrderStatus status, Pageable pageable) {
        return orderRepository.findBySellerIdAndStatus(sellerId, status, pageable)
                .map(order -> enrichOrderVO(OrderVO.fromEntity(order)));
    }
    
    /**
     * 卖家确认订单
     */
    @Transactional
    public OrderVO confirmOrder(Long orderId, Long sellerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (!order.canConfirm()) {
            throw new BusinessException("订单状态不允许确认");
        }
        
        // Saga确认预占
        ApiResponse<Boolean> confirmResp = productClient.confirmReserve(order.getProductId(), order.getOrderNo());
        if (confirmResp.getCode() != 200 || !Boolean.TRUE.equals(confirmResp.getData())) {
            throw new BusinessException("商品预占确认失败");
        }
        
        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        order = orderRepository.save(order);
        
        // 通知买家
        sendOrderNotification(order.getBuyerId(), order, "订单已确认", 
                "卖家已确认你的订单「" + order.getProductTitle() + "」，等待发货～");
        
        log.info("Order confirmed: orderId={}", orderId);
        return OrderVO.fromEntity(order);
    }
    
    /**
     * 卖家发货
     */
    @Transactional
    public OrderVO shipOrder(Long orderId, Long sellerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (!order.canShip()) {
            throw new BusinessException("订单状态不允许发货");
        }
        
        order.setStatus(OrderStatus.SHIPPED);
        order.setShippedAt(LocalDateTime.now());
        order = orderRepository.save(order);
        
        // 通知买家
        sendOrderNotification(order.getBuyerId(), order, "卖家已发货", 
                "「" + order.getProductTitle() + "」已发货，请注意查收～");
        
        log.info("Order shipped: orderId={}", orderId);
        return OrderVO.fromEntity(order);
    }
    
    /**
     * 买家确认收货
     */
    @Transactional
    public OrderVO completeOrder(Long orderId, Long buyerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (!order.canComplete()) {
            throw new BusinessException("订单状态不允许确认收货");
        }
        
        // 标记商品为已售出
        productClient.markSold(order.getProductId(), order.getOrderNo());
        
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        order = orderRepository.save(order);
        
        // 增加卖家信誉分
        try {
            userClient.updateReputation(order.getSellerId(), 3);
        } catch (Exception e) {
            log.warn("Failed to update seller reputation: {}", e.getMessage());
        }
        
        // 通知卖家
        sendOrderNotification(order.getSellerId(), order, "交易完成", 
                "买家已确认收货，「" + order.getProductTitle() + "」交易完成！");
        
        log.info("Order completed: orderId={}", orderId);
        return OrderVO.fromEntity(order);
    }
    
    /**
     * 取消订单
     */
    @Transactional
    public OrderVO cancelOrder(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (!order.canCancel()) {
            throw new BusinessException("订单状态不允许取消");
        }
        
        // Saga补偿：取消预占
        productClient.cancelReserve(order.getProductId(), order.getOrderNo());
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        order = orderRepository.save(order);
        
        // 通知对方
        Long notifyUserId = userId.equals(order.getBuyerId()) ? order.getSellerId() : order.getBuyerId();
        sendOrderNotification(notifyUserId, order, "订单已取消", 
                "订单「" + order.getProductTitle() + "」已被取消");
        
        log.info("Order cancelled: orderId={}, reason={}", orderId, reason);
        return OrderVO.fromEntity(order);
    }
    
    // ========== 私有方法 ==========
    
    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = orderSeq.incrementAndGet() % 10000;
        return String.format("ORD%s%04d", date, seq);
    }
    
    private OrderVO enrichOrderVO(OrderVO vo) {
        try {
            List<Long> userIds = Arrays.asList(vo.getBuyerId(), vo.getSellerId());
            Map<String, List<Long>> req = new HashMap<>();
            req.put("ids", userIds);
            
            ApiResponse<List<Map<String, Object>>> resp = userClient.getUserBriefs(req);
            if (resp.getCode() == 200 && resp.getData() != null) {
                for (Map<String, Object> user : resp.getData()) {
                    Long id = Long.valueOf(user.get("id").toString());
                    String username = (String) user.get("username");
                    if (id.equals(vo.getBuyerId())) {
                        vo.setBuyerName(username);
                    }
                    if (id.equals(vo.getSellerId())) {
                        vo.setSellerName(username);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to enrich order with user names: {}", e.getMessage());
        }
        return vo;
    }
    
    private void sendOrderNotification(Long userId, Order order, String title, String content) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("userId", userId);
            message.put("type", "ORDER");
            message.put("title", title);
            message.put("content", content);
            message.put("relatedId", order.getId());
            notifyClient.sendMessage(message);
        } catch (Exception e) {
            log.warn("Failed to send order notification: {}", e.getMessage());
        }
    }
    
    /**
     * 管理员获取所有订单列表
     */
    public Page<OrderVO> listAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(order -> enrichOrderVO(OrderVO.fromEntity(order)));
    }
    
    /**
     * 管理员获取订单详情
     */
    public OrderVO getOrderByIdForAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return enrichOrderVO(OrderVO.fromEntity(order));
    }
}
