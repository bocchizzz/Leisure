package com.bangboo.marketplace.service;

import com.bangboo.common.exception.ForbiddenException;
import com.bangboo.common.exception.UnauthorizedException;
import com.bangboo.common.security.CurrentUser;
import com.bangboo.common.security.CurrentUserContext;
import com.bangboo.marketplace.client.InternalApiClient;
import com.bangboo.marketplace.dto.internal.UserAccessStateVO;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * marketplace 鉴权与资格校验。
 * 当前用户来自 Gateway 透传的 X-User-* 请求头（由 bb-common HeaderAuthenticationFilter 注入）。
 */
@Service
public class MarketplaceAuthService {
    private final InternalApiClient internalApiClient;

    public MarketplaceAuthService(InternalApiClient internalApiClient) {
        this.internalApiClient = internalApiClient;
    }

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

    /**
     * 发布/申请等关键操作前校验账号状态与校园认证。
     * 优先以 IAM access-state 二次确认；IAM 不可用时回退到网关签发 JWT 中的状态（可信来源）。
     */
    public CurrentUser requireActiveVerifiedUser() {
        CurrentUser user = requireUser();
        Optional<UserAccessStateVO> state = internalApiClient.userAccessState(user.uid());
        String status = state.map(UserAccessStateVO::status).orElse(user.status());
        boolean campusVerified = state.map(s -> Boolean.TRUE.equals(s.campusVerified()))
                .orElse(user.campusVerified());

        if ("BANNED".equalsIgnoreCase(status)) {
            throw new ForbiddenException("账号已被封禁");
        }
        if (!campusVerified) {
            throw new ForbiddenException("请先完成校园认证");
        }
        return user;
    }
}
