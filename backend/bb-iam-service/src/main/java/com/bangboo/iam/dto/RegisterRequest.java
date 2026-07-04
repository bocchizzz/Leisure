package com.bangboo.iam.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "请输入用户名") String username,
        @NotBlank(message = "请输入密码") @Size(min = 6, message = "密码至少 6 位") String password,
        @Email(message = "邮箱格式不正确") String email,
        String nickname
) {
}
