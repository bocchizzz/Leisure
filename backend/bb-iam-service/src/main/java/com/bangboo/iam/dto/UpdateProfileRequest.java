package com.bangboo.iam.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateProfileRequest(
        String nickname,
        @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确") String phone,
        String school,
        String avatarUrl,
        @Email(message = "邮箱格式不正确") String email
) {
}
