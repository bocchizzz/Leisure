package com.bangboo.ai.service;

import com.bangboo.common.exception.UnauthorizedException;
import com.bangboo.common.security.CurrentUser;
import com.bangboo.common.security.CurrentUserContext;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * AI 鉴权。/ai/* 需要登录（分工 9.10）。
 * 当前用户来自 Gateway 透传的 X-User-* 请求头。
 */
@Service
public class AiAuthService {

    public CurrentUser requireUser() {
        return CurrentUserContext.get().orElseThrow(() -> new UnauthorizedException("请先登录"));
    }

    public Optional<CurrentUser> optionalUser() {
        return CurrentUserContext.get();
    }
}
