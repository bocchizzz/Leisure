package com.bangboo.common.security;

import java.util.List;

public record CurrentUser(
        Long uid,
        List<String> roles,
        String status,
        boolean campusVerified
) {
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
