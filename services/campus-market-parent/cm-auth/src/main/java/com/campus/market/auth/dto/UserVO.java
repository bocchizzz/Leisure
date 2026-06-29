package com.campus.market.auth.dto;

import com.campus.market.auth.entity.User;
import com.campus.market.auth.enums.UserRole;
import com.campus.market.auth.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户视图对象
 */
@Data
public class UserVO {
    
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private Set<String> roles;
    private UserStatus status;
    private Integer reputation;
    private String reputationLevel;
    private LocalDateTime createdAt;
    
    public static UserVO fromEntity(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setRoles(user.getRoles().stream()
                .map(UserRole::name)
                .collect(Collectors.toSet()));
        vo.setStatus(user.getStatus());
        vo.setReputation(user.getReputation());
        vo.setReputationLevel(user.getReputationLevel());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
