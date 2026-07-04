package com.bangboo.reputation.service;

import com.bangboo.common.exception.ForbiddenException;
import com.bangboo.common.exception.UnauthorizedException;
import com.bangboo.common.security.CurrentUser;
import com.bangboo.common.security.CurrentUserContext;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * reputation 鉴权。
 * 当前用户来自 Gateway 透传的 X-User-* 请求头（由 bb-common HeaderAuthenticationFilter 注入）。
 */
@Service
public class ReputationAuthService {

    public CurrentUser requireUser() {
        return CurrentUserContext.get().orElseThrow(() -> new UnauthorizedException("请先登录"));
    }

    public Optional<CurrentUser> optionalUser() {
        return CurrentUserContext.get();
    }

    public boolean isAdmin(CurrentUser user) {
        return user != null && (user.hasRole("ADMIN") || user.hasRole("SUPER_ADMIN"));
    }

    public CurrentUser requireAdmin() {
        CurrentUser user = requireUser();
        if (!isAdmin(user)) {
            throw new ForbiddenException("需要管理员权限");
        }
        return user;
    }
}
