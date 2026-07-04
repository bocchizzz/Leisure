package com.bangboo.message.controller;

import com.bangboo.common.exception.UnauthorizedException;
import com.bangboo.common.response.PageResult;
import com.bangboo.common.security.CurrentUser;
import com.bangboo.common.security.CurrentUserContext;
import com.bangboo.message.dto.MessageVO;
import com.bangboo.message.service.MessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public PageResult<MessageVO> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead
    ) {
        CurrentUser user = requireUser();
        return messageService.list(user.uid(), page, size, type, isRead);
    }

    @GetMapping("/unread-count")
    public Long unreadCount() {
        CurrentUser user = requireUser();
        return messageService.unreadCount(user.uid());
    }

    @PutMapping("/{id}/read")
    public Void markRead(@PathVariable Long id) {
        CurrentUser user = requireUser();
        messageService.markRead(user.uid(), id);
        return null;
    }

    @PutMapping("/read-all")
    public Void markAllRead() {
        CurrentUser user = requireUser();
        messageService.markAllRead(user.uid());
        return null;
    }

    private static CurrentUser requireUser() {
        return CurrentUserContext.get().orElseThrow(() -> new UnauthorizedException("请先登录"));
    }
}
