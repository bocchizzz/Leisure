package com.campus.market.notify.service;

import com.campus.market.notify.entity.Message;
import com.campus.market.notify.enums.MessageType;
import com.campus.market.notify.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消息服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {
    
    private final MessageRepository messageRepository;
    
    /**
     * 获取用户消息列表
     */
    public Page<Message> getMessages(Long userId, Boolean unreadOnly, Pageable pageable) {
        if (Boolean.TRUE.equals(unreadOnly)) {
            return messageRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false, pageable);
        }
        return messageRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * 获取未读消息数
     */
    public int getUnreadCount(Long userId) {
        return messageRepository.countUnreadByUserId(userId);
    }
    
    /**
     * 标记消息为已读
     */
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        messageRepository.markAsRead(messageId, userId);
    }
    
    /**
     * 标记所有消息为已读
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        messageRepository.markAllAsRead(userId);
        log.info("All messages marked as read for user: {}", userId);
    }
    
    /**
     * 发送消息（内部接口）
     */
    @Transactional
    public void sendMessage(Long userId, MessageType type, String title, String content, Long relatedId) {
        Message message = new Message();
        message.setUserId(userId);
        message.setType(type);
        message.setTitle(title);
        message.setContent(content);
        message.setRelatedId(relatedId);
        message.setIsRead(false);
        
        messageRepository.save(message);
        log.info("Message sent: userId={}, type={}, title={}", userId, type, title);
    }
}
