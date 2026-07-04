package com.bangboo.iam.dto;

import com.bangboo.iam.enums.UserRole;
import com.bangboo.iam.enums.UserStatus;

import java.time.Instant;
import java.util.List;

public record UserVO(
        Long id,
        String username,
        String email,
        String nickname,
        String phone,
        String school,
        String avatarUrl,
        UserStatus status,
        int reputation,
        String reputationLevel,
        List<UserRole> roles,
        boolean campusVerified,
        Integer hunterLevel,
        String hunterTitle,
        Instant createdAt
) {
}
