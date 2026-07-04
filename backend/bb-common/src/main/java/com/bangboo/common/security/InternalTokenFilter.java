package com.bangboo.common.security;

import com.bangboo.common.constant.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class InternalTokenFilter extends OncePerRequestFilter {
    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final String internalToken;

    public InternalTokenFilter(String internalToken) {
        this.internalToken = internalToken;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String actualToken = request.getHeader(INTERNAL_TOKEN_HEADER);
        if (internalToken != null && internalToken.equals(actualToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(ErrorCode.FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"内部接口访问被拒绝\",\"data\":null}");
    }
}
