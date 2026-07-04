package com.bangboo.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class HeaderAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        CurrentUser user = fromHeaders(request);
        if (user != null) {
            CurrentUserContext.set(user);
        } else {
            CurrentUserContext.clear();
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            CurrentUserContext.clear();
        }
    }

    private static CurrentUser fromHeaders(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            return null;
        }
        String roles = request.getHeader("X-User-Roles");
        String status = request.getHeader("X-User-Status");
        String campusVerified = request.getHeader("X-Campus-Verified");
        return new CurrentUser(
                Long.valueOf(userId),
                splitRoles(roles),
                status == null ? "" : status,
                Boolean.parseBoolean(campusVerified)
        );
    }

    private static List<String> splitRoles(String roles) {
        if (roles == null || roles.isBlank()) {
            return List.of();
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .toList();
    }
}
