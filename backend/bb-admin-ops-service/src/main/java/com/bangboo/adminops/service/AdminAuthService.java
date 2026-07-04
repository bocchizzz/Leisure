package com.bangboo.adminops.service;

import com.bangboo.common.exception.ForbiddenException;
import com.bangboo.common.exception.UnauthorizedException;
import com.bangboo.common.security.CurrentUser;
import com.bangboo.common.security.CurrentUserContext;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {
    public CurrentUser requireAdmin() {
        CurrentUser user = CurrentUserContext.get()
                .orElseThrow(() -> new UnauthorizedException("请先登录"));
        if (!user.hasRole("ADMIN") && !user.hasRole("SUPER_ADMIN")) {
            throw new ForbiddenException("需要管理员权限");
        }
        return user;
    }
}
