package com.bangboo.iam.dto;

import com.bangboo.iam.enums.UserRole;
import com.bangboo.iam.enums.UserStatus;

import java.util.List;

public record UserAccessStateVO(
        Long userId,
        UserStatus status,
        boolean campusVerified,
        List<UserRole> roles
) {
}
