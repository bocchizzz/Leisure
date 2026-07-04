package com.bangboo.message.controller;

import com.bangboo.message.dto.CreateMessageRequest;
import com.bangboo.message.dto.MessageVO;
import com.bangboo.message.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/messages")
public class InternalMessageController {
    private final MessageService messageService;

    public InternalMessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public MessageVO create(@Valid @RequestBody CreateMessageRequest request) {
        return messageService.create(request);
    }
}
