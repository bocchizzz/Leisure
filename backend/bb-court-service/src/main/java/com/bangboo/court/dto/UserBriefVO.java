package com.bangboo.court.dto;

import java.util.List;

public record UserBriefVO(
        Long id,
        String username,
        String nickname,
        String avatarUrl,
        String status,
        boolean campusVerified,
        int reputation,
        List<String> roles
) {
    public boolean isAdmin() {
        return roles != null && (roles.contains("ADMIN") || roles.contains("SUPER_ADMIN"));
    }
}
