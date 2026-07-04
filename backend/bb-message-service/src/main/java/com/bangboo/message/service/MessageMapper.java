package com.bangboo.message.service;

import com.bangboo.message.dto.MessageVO;
import com.bangboo.message.entity.Message;

public final class MessageMapper {
    private MessageMapper() {
    }

    public static MessageVO toVO(Message message) {
        return new MessageVO(
                message.getId(),
                message.getUserId(),
                message.getType(),
                message.getTitle(),
                message.getContent(),
                message.isRead(),
                message.getRelatedId(),
                message.getCreatedAt()
        );
    }
}
