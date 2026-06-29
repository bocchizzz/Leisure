package com.campus.market.product.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.common.security.SecurityUtils;
import com.campus.market.product.dto.ProductVO;
import com.campus.market.product.enums.ProductStatus;
import com.campus.market.product.feign.AuthClient;
import com.campus.market.product.feign.NotifyClient;
import com.campus.market.product.feign.TradeClient;
import com.campus.market.product.service.AIService;
import com.campus.market.product.service.FavoriteService;
import com.campus.market.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 智能服务控制器
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI智能服务", description = "智能描述生成、定价建议、商品问答等")
public class AIController {

    private final AIService aiService;
    private final ProductService productService;
    private final FavoriteService favoriteService;
    
    @Autowired(required = false)
    private AuthClient authClient;
    
    @Autowired(required = false)
    private TradeClient tradeClient;
    
    @Autowired(required = false)
    private NotifyClient notifyClient;

    @PostMapping("/generate-description")
    @Operation(summary = "AI生成商品描述")
    public ApiResponse<String> generateDescription(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String category = request.get("category");
        String condition = request.get("condition");
        
        if (title == null || title.isEmpty()) {
            return ApiResponse.error(400, "请提供商品标题");
        }
        
        String description = aiService.generateProductDescription(
            title, 
            category != null ? category : "其他",
            condition != null ? condition : "良好"
        );
        return ApiResponse.success(description);
    }

    @PostMapping("/price-suggestion")
    @Operation(summary = "AI定价建议")
    public ApiResponse<Map<String, Object>> priceSuggestion(@RequestBody Map<String, Object> request) {
        String title = (String) request.get("title");
        String category = (String) request.get("category");
        String condition = (String) request.get("condition");
        Double marketAvgPrice = request.get("marketAvgPrice") != null ? 
            ((Number) request.get("marketAvgPrice")).doubleValue() : null;
        Double marketMinPrice = request.get("marketMinPrice") != null ? 
            ((Number) request.get("marketMinPrice")).doubleValue() : null;
        Double marketMaxPrice = request.get("marketMaxPrice") != null ? 
            ((Number) request.get("marketMaxPrice")).doubleValue() : null;
        
        Map<String, Object> suggestion = aiService.getPriceSuggestion(
            title, category, condition, marketAvgPrice, marketMinPrice, marketMaxPrice
        );
        return ApiResponse.success(suggestion);
    }

    @PostMapping("/ask-product")
    @Operation(summary = "商品问答助手")
    public ApiResponse<String> askAboutProduct(@RequestBody Map<String, Object> request) {
        String question = (String) request.get("question");
        Long productId = request.get("productId") != null ? 
            ((Number) request.get("productId")).longValue() : null;
        
        if (question == null || question.isEmpty()) {
            return ApiResponse.error(400, "请输入问题");
        }
        
        String productTitle = "商品";
        String productDescription = "";
        String category = "";
        Double price = 0.0;
        
        if (productId != null) {
            try {
                ProductVO product = productService.getProductById(productId);
                productTitle = product.getTitle();
                productDescription = product.getDescription();
                category = product.getCategory().name();
                price = product.getPrice().doubleValue();
            } catch (Exception e) {
                // 商品不存在，使用默认值
            }
        }
        
        String answer = aiService.askAboutProduct(question, productTitle, productDescription, category, price);
        return ApiResponse.success(answer);
    }

    @PostMapping("/assess-risk")
    @Operation(summary = "交易风险评估")
    public ApiResponse<Map<String, Object>> assessRisk(@RequestBody Map<String, Object> request) {
        String productTitle = (String) request.get("productTitle");
        Double price = request.get("price") != null ? 
            ((Number) request.get("price")).doubleValue() : 0.0;
        Integer sellerReputation = request.get("sellerReputation") != null ? 
            ((Number) request.get("sellerReputation")).intValue() : 100;
        Integer sellerTransactions = request.get("sellerTransactions") != null ? 
            ((Number) request.get("sellerTransactions")).intValue() : 0;
        
        Map<String, Object> assessment = aiService.assessTradeRisk(
            productTitle, price, sellerReputation, sellerTransactions
        );
        return ApiResponse.success(assessment);
    }

    @PostMapping("/smart-search")
    @Operation(summary = "智能搜索意图解析")
    public ApiResponse<Map<String, Object>> smartSearch(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        if (query == null || query.isEmpty()) {
            return ApiResponse.error(400, "请输入搜索内容");
        }
        
        Map<String, Object> intent = aiService.parseSearchIntent(query);
        return ApiResponse.success(intent);
    }

    @PostMapping("/customer-service")
    @Operation(summary = "智能客服（基础版）")
    public ApiResponse<String> customerService(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.isEmpty()) {
            return ApiResponse.error(400, "请输入问题");
        }
        
        String answer = aiService.customerService(question);
        return ApiResponse.success(answer);
    }
    
    @PostMapping("/smart-assistant")
    @Operation(summary = "智能助手（增强版，100%了解系统和用户）")
    public ApiResponse<Map<String, Object>> smartAssistant(@RequestBody Map<String, Object> request) {
        String question = (String) request.get("question");
        if (question == null || question.isEmpty()) {
            return ApiResponse.error(400, "请输入问题");
        }
        
        // 获取对话历史
        @SuppressWarnings("unchecked")
        List<Map<String, String>> conversationHistory = (List<Map<String, String>>) request.get("history");
        
        // 构建完整的系统和用户上下文
        StringBuilder fullContext = new StringBuilder();
        
        // 1. 当前用户信息（从前端传入）
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String currentUsername = SecurityUtils.getCurrentUsername();
        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) request.get("userRoles");
        Integer userReputation = request.get("userReputation") != null ? 
            ((Number) request.get("userReputation")).intValue() : 100;
        String userNickname = (String) request.get("userNickname");
        String userStatus = (String) request.get("userStatus");
        
        // 从前端传入的订单信息
        String buyerOrdersInfo = (String) request.get("buyerOrders");
        String sellerOrdersInfo = (String) request.get("sellerOrders");
        
        // 提取角色变量（在整个方法中使用）
        final boolean isBuyer = userRoles != null && userRoles.contains("BUYER");
        final boolean isSeller = userRoles != null && userRoles.contains("SELLER");
        final boolean isAdmin = userRoles != null && userRoles.contains("ADMIN");
        
        fullContext.append("【当前登录用户信息】\n");
        if (currentUserId != null) {
            fullContext.append("- 用户名：").append(currentUsername).append("\n");
            if (userNickname != null && !userNickname.isEmpty()) {
                fullContext.append("- 昵称：").append(userNickname).append("\n");
            }
            
            // 明确标注用户拥有和缺少的角色
            fullContext.append("- 拥有的角色：");
            if (userRoles != null && !userRoles.isEmpty()) {
                List<String> roleNames = new ArrayList<>();
                if (isBuyer) roleNames.add("买家(BUYER)");
                if (isSeller) roleNames.add("卖家(SELLER)");
                if (isAdmin) roleNames.add("管理员(ADMIN)");
                fullContext.append(roleNames.isEmpty() ? "无" : String.join("、", roleNames));
            } else {
                fullContext.append("无");
            }
            fullContext.append("\n");
            
            // 明确标注缺少的角色（这对AI判断权限非常重要）
            List<String> missingRoles = new ArrayList<>();
            if (!isBuyer) missingRoles.add("买家(BUYER)");
            if (!isSeller) missingRoles.add("卖家(SELLER)");
            if (!isAdmin) missingRoles.add("管理员(ADMIN)");
            if (!missingRoles.isEmpty()) {
                fullContext.append("- 缺少的角色（无权执行对应操作）：").append(String.join("、", missingRoles)).append("\n");
            }
            
            fullContext.append("- 信誉分：").append(userReputation).append("分\n");
            if (userStatus != null) {
                fullContext.append("- 账号状态：").append("ACTIVE".equals(userStatus) ? "正常" : userStatus).append("\n");
            }
            fullContext.append("- 用户ID：").append(currentUserId).append("（内部使用，不要告诉用户）\n");
        } else {
            fullContext.append("- 未登录（无任何权限，只能浏览商品）\n");
        }
        
        // 2. 用户收藏的商品（仅买家可见）
        fullContext.append("\n【用户收藏的商品】\n");
        if (!isBuyer) {
            fullContext.append("- 无买家身份，无法查看收藏\n");
        } else if (currentUserId != null) {
            try {
                Page<ProductVO> favorites = favoriteService.listFavorites(currentUserId, PageRequest.of(0, 20));
                if (favorites.hasContent()) {
                    for (ProductVO fav : favorites.getContent()) {
                        fullContext.append(String.format("- [收藏]「%s」%.0f元 (id:%d, 状态:%s)\n", 
                            fav.getTitle(), fav.getPrice().doubleValue(), fav.getId(), 
                            getStatusName(fav.getStatus())));
                    }
                } else {
                    fullContext.append("- 暂无收藏\n");
                }
            } catch (Exception e) {
                fullContext.append("- 获取失败\n");
                log.warn("Failed to get favorites: {}", e.getMessage());
            }
        } else {
            fullContext.append("- 未登录，无法查看\n");
        }
        
        // 3. 用户发布的商品（仅卖家可见）
        fullContext.append("\n【用户发布的商品（自己的商品，不能推荐给自己）】\n");
        if (!isSeller) {
            fullContext.append("- 无卖家身份，无法查看发布的商品\n");
        } else if (currentUserId != null) {
            try {
                Page<ProductVO> myProducts = productService.getMyProducts(currentUserId, null, PageRequest.of(0, 20));
                if (myProducts.hasContent()) {
                    for (ProductVO p : myProducts.getContent()) {
                        fullContext.append(String.format("- [我的]「%s」%.0f元 (id:%d, 状态:%s)\n", 
                            p.getTitle(), p.getPrice().doubleValue(), p.getId(),
                            getStatusName(p.getStatus())));
                    }
                } else {
                    fullContext.append("- 暂无发布的商品\n");
                }
            } catch (Exception e) {
                fullContext.append("- 获取失败\n");
                log.warn("Failed to get my products: {}", e.getMessage());
            }
        } else {
            fullContext.append("- 未登录，无法查看\n");
        }
        
        // 4. 用户的购买订单（仅买家可见）
        fullContext.append("\n【用户的购买订单】\n");
        if (!isBuyer) {
            fullContext.append("- 无买家身份，无法查看购买订单\n");
        } else if (buyerOrdersInfo != null && !buyerOrdersInfo.isEmpty()) {
            fullContext.append(buyerOrdersInfo).append("\n");
        } else {
            fullContext.append("- 暂无购买订单\n");
        }
        
        // 5. 用户的销售订单（仅卖家可见）
        fullContext.append("\n【用户的销售订单】\n");
        if (!isSeller) {
            fullContext.append("- 无卖家身份，无法查看销售订单\n");
        } else if (sellerOrdersInfo != null && !sellerOrdersInfo.isEmpty()) {
            fullContext.append(sellerOrdersInfo).append("\n");
        } else {
            fullContext.append("- 暂无销售订单\n");
        }
        
        // 6. 平台在售商品（排除用户自己的商品）
        fullContext.append("\n【平台在售商品（可推荐给用户购买）】\n");
        try {
            Page<ProductVO> products = productService.listProducts(
                null, null, null, null, null, null, ProductStatus.AVAILABLE,
                PageRequest.of(0, 30)
            );
            
            List<ProductVO> availableProducts = products.getContent().stream()
                .filter(p -> currentUserId == null || !currentUserId.equals(p.getSellerId()))
                .collect(Collectors.toList());
            
            if (!availableProducts.isEmpty()) {
                for (ProductVO p : availableProducts) {
                    fullContext.append(String.format("- [在售]「%s」%.0f元 [%s] 卖家:%s (id:%d)\n", 
                        p.getTitle(), p.getPrice().doubleValue(), 
                        getCategoryName(p.getCategory().name()),
                        p.getSellerName() != null ? p.getSellerName() : "未知",
                        p.getId()));
                }
            } else {
                fullContext.append("- 暂无在售商品\n");
            }
        } catch (Exception e) {
            fullContext.append("- 获取失败\n");
            log.warn("Failed to get products: {}", e.getMessage());
        }
        
        // 7. 平台所有用户列表（仅管理员可见）
        log.info("AI智能助手 - isAdmin={}, authClient={}", isAdmin, authClient != null ? "已注入" : "NULL");
        if (isAdmin && authClient != null) {
            fullContext.append("\n【平台所有用户（管理员可见）】\n");
            try {
                log.info("AI智能助手 - 正在调用authClient.getAllUsers()...");
                ApiResponse<List<Map<String, Object>>> usersResp = authClient.getAllUsers();
                log.info("AI智能助手 - authClient.getAllUsers() 返回: code={}, dataSize={}", 
                    usersResp.getCode(), usersResp.getData() != null ? usersResp.getData().size() : 0);
                if (usersResp.getCode() == 200 && usersResp.getData() != null) {
                    for (Map<String, Object> user : usersResp.getData()) {
                        fullContext.append(String.format("- 用户「%s」状态:%s 信誉:%s分 角色:%s (id:%s)\n",
                            user.get("username"),
                            user.get("statusName"),
                            user.get("reputation"),
                            user.get("roles"),
                            user.get("id")));
                    }
                }
            } catch (Exception e) {
                fullContext.append("- 获取失败\n");
                log.warn("Failed to get all users: {}", e.getMessage(), e);
            }
        }
        
        // 8. 平台所有订单（仅管理员可见）
        if (isAdmin && tradeClient != null) {
            fullContext.append("\n【平台所有订单（管理员可见）】\n");
            try {
                ApiResponse<List<Map<String, Object>>> ordersResp = tradeClient.getAllOrders();
                if (ordersResp.getCode() == 200 && ordersResp.getData() != null) {
                    for (Map<String, Object> order : ordersResp.getData()) {
                        fullContext.append(String.format("- 订单「%s」状态:%s 金额:%s元 买家id:%s 卖家id:%s (id:%s)\n",
                            order.get("productTitle"),
                            order.get("statusName"),
                            order.get("totalPrice"),
                            order.get("buyerId"),
                            order.get("sellerId"),
                            order.get("id")));
                    }
                }
            } catch (Exception e) {
                fullContext.append("- 获取失败\n");
                log.warn("Failed to get all orders: {}", e.getMessage());
            }
        }
        
        // 9. 平台所有申诉（仅管理员可见）
        if (isAdmin && notifyClient != null) {
            fullContext.append("\n【平台所有申诉（管理员可见）】\n");
            try {
                ApiResponse<List<Map<String, Object>>> appealsResp = notifyClient.getAllAppeals();
                if (appealsResp.getCode() == 200 && appealsResp.getData() != null) {
                    if (appealsResp.getData().isEmpty()) {
                        fullContext.append("- 暂无申诉\n");
                    } else {
                        for (Map<String, Object> appeal : appealsResp.getData()) {
                            fullContext.append(String.format("- 申诉 类型:%s 状态:%s 用户id:%s (id:%s)\n",
                                appeal.get("type"),
                                appeal.get("statusName"),
                                appeal.get("userId"),
                                appeal.get("id")));
                        }
                    }
                }
            } catch (Exception e) {
                fullContext.append("- 获取失败\n");
                log.warn("Failed to get all appeals: {}", e.getMessage());
            }
        }
        
        // 调用 AI
        Map<String, Object> result = aiService.smartAssistant(question, fullContext.toString(), conversationHistory);
        return ApiResponse.success(result);
    }
    
    private String getStatusName(ProductStatus status) {
        if (status == null) return "未知";
        switch (status) {
            case AVAILABLE: return "在售";
            case RESERVED: return "已预订";
            case SOLD: return "已售出";
            case REMOVED: return "已下架";
            default: return status.name();
        }
    }
    
    private String getCategoryName(String category) {
        if (category == null) return "其他";
        switch (category) {
            case "ELECTRONICS": return "数码电子";
            case "BOOKS": return "图书教材";
            case "CLOTHING": return "服饰鞋包";
            case "DAILY": return "日用百货";
            case "SPORTS": return "运动户外";
            case "BEAUTY": return "美妆护肤";
            case "FOOD": return "食品饮料";
            case "OTHER": return "其他";
            default: return category;
        }
    }
}
