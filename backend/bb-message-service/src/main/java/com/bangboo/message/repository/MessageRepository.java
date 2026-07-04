package com.bangboo.message.repository;

import com.bangboo.message.entity.Message;
import com.bangboo.message.enums.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByUserId(Long userId, Pageable pageable);

    Page<Message> findByUserIdAndType(Long userId, MessageType type, Pageable pageable);

    Page<Message> findByUserIdAndRead(Long userId, boolean read, Pageable pageable);

    Page<Message> findByUserIdAndTypeAndRead(Long userId, MessageType type, boolean read, Pageable pageable);

    long countByUserIdAndReadFalse(Long userId);

    List<Message> findByUserIdAndReadFalse(Long userId);
}
