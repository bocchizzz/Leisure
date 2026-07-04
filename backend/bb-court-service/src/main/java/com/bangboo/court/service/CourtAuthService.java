package com.bangboo.court.service;

import com.bangboo.common.exception.ForbiddenException;
import com.bangboo.common.exception.UnauthorizedException;
import com.bangboo.common.security.CurrentUser;
import com.bangboo.common.security.CurrentUserContext;
import org.springframework.stereotype.Service;

@Service
public class CourtAuthService {
    public CurrentUser requireUser() {
        return CurrentUserContext.get().orElseThrow(() -> new UnauthorizedException("请先登录"));
    }

    public CurrentUser requireAdmin() {
        CurrentUser user = requireUser();
        if (!isAdmin(user)) {
            throw new ForbiddenException("需要管理员权限");
        }
        return user;
    }

    public boolean isAdmin(CurrentUser user) {
        return user.hasRole("ADMIN") || user.hasRole("SUPER_ADMIN");
    }
}
