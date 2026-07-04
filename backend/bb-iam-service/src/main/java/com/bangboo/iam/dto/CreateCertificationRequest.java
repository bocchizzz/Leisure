package com.bangboo.iam.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCertificationRequest(
        @NotBlank(message = "真实姓名不能为空") String realName,
        @NotBlank(message = "学校不能为空") String school,
        @NotBlank(message = "学号不能为空") String studentNo,
        @NotBlank(message = "认证材料不能为空") String materialUrl
) {
}
