package com.campus.market.notify.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.common.security.SecurityUtils;
import com.campus.market.notify.entity.Message;
import com.campus.market.notify.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "消息接口", description = "用户消息管理")
public class MessageController {
    
    private final MessageService messageService;
    
    @GetMapping
    @Operation(summary = "消息列表")
    public ApiResponse<Page<Message>> getMessages(
            @RequestParam(required = false) Boolean unreadOnly,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<Message> messages = messageService.getMessages(userId, unreadOnly, pageable);
        return ApiResponse.success(messages);
    }
    
    @GetMapping("/unread-count")
    @Operation(summary = "未读消息数")
    public ApiResponse<Integer> getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        int count = messageService.getUnreadCount(userId);
        return ApiResponse.success(count);
    }
    
    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读")
    public ApiResponse<Void> markAsRead(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        messageService.markAsRead(id, userId);
        return ApiResponse.success(null);
    }
    
    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读")
    public ApiResponse<Void> markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        messageService.markAllAsRead(userId);
        return ApiResponse.success(null);
    }
}
