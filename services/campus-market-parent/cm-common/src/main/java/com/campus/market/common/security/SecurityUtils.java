package com.campus.market.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 安全工具类：从SecurityContext获取当前用户信息
 */
public class SecurityUtils {
    
    private SecurityUtils() {
        // 工具类禁止实例化
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails) {
            return ((JwtUserDetails) authentication.getPrincipal()).getUserId();
        }
        return null;
    }
    
    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails) {
            return ((JwtUserDetails) authentication.getPrincipal()).getUsername();
        }
        return null;
    }
    
    /**
     * 获取当前用户角色列表
     */
    public static List<String> getCurrentRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
    
    /**
     * 判断当前用户是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof JwtUserDetails;
    }
    
    /**
     * 判断当前用户是否具有指定角色
     */
    public static boolean hasRole(String role) {
        return getCurrentRoles().contains(role);
    }
    
    /**
     * 判断当前用户是否为管理员
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
