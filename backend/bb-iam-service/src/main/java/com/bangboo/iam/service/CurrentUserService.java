package com.bangboo.iam.service;

import com.bangboo.common.exception.ForbiddenException;
import com.bangboo.common.exception.UnauthorizedException;
import com.bangboo.common.security.CurrentUser;
import com.bangboo.common.security.CurrentUserContext;
import com.bangboo.iam.enums.UserRole;
import com.bangboo.iam.enums.UserStatus;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    public CurrentUser requireUser() {
        CurrentUser user = CurrentUserContext.get()
                .orElseThrow(() -> new UnauthorizedException("请先登录"));
        if (UserStatus.BANNED.name().equals(user.status())) {
            throw new ForbiddenException("账号已被封禁");
        }
        return user;
    }

    public CurrentUser requireAdmin() {
        CurrentUser user = requireUser();
        if (!user.hasRole(UserRole.ADMIN.name()) && !user.hasRole(UserRole.SUPER_ADMIN.name())) {
            throw new ForbiddenException("需要管理员权限");
        }
        return user;
    }
}
