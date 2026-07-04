package com.bangboo.message.service;

import com.bangboo.common.constant.ErrorCode;
import com.bangboo.common.exception.BusinessException;
import com.bangboo.common.exception.ForbiddenException;
import com.bangboo.common.exception.ResourceNotFoundException;
import com.bangboo.common.response.PageResult;
import com.bangboo.message.dto.CreateMessageRequest;
import com.bangboo.message.dto.MessageVO;
import com.bangboo.message.entity.Message;
import com.bangboo.message.enums.MessageType;
import com.bangboo.message.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<MessageVO> list(Long userId, int page, int size, String type, Boolean isRead) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizeSize(size), Sort.by(Sort.Direction.DESC, "createdAt"));
        MessageType typeFilter = parseType(type);
        Page<Message> result = queryMessages(userId, typeFilter, isRead, pageable);
        List<MessageVO> content = result.getContent().stream().map(MessageMapper::toVO).toList();
        return new PageResult<>(
                content,
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize(),
                result.isFirst(),
                result.isLast()
        );
    }

    private Page<Message> queryMessages(Long userId, MessageType type, Boolean isRead, Pageable pageable) {
        if (type != null && isRead != null) {
            return messageRepository.findByUserIdAndTypeAndRead(userId, type, isRead, pageable);
        }
        if (type != null) {
            return messageRepository.findByUserIdAndType(userId, type, pageable);
        }
        if (isRead != null) {
            return messageRepository.findByUserIdAndRead(userId, isRead, pageable);
        }
        return messageRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return messageRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(Long userId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("消息不存在"));
        if (!message.getUserId().equals(userId)) {
            throw new ForbiddenException("只能操作自己的消息");
        }
        message.setRead(true);
    }

    @Transactional
    public void markAllRead(Long userId) {
        messageRepository.findByUserIdAndReadFalse(userId).forEach(message -> message.setRead(true));
    }

    @Transactional
    public MessageVO create(CreateMessageRequest request) {
        Message message = new Message();
        message.setUserId(request.userId());
        message.setType(request.type());
        message.setTitle(request.title().trim());
        message.setContent(request.content().trim());
        message.setRelatedId(request.relatedId());
        return MessageMapper.toVO(messageRepository.save(message));
    }

    private static MessageType parseType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return MessageType.valueOf(type.trim());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息类型不支持");
        }
    }

    private static int normalizeSize(int size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }
}
