package com.bangboo.marketplace.dto.internal;

import java.util.List;

/** IAM 用户访问状态，用于发布/申请前二次校验。对齐 A 侧 /internal/users/{id}/access-state。 */
public record UserAccessStateVO(
        Long userId,
        String status,
        Boolean campusVerified,
        List<String> roles
) {
}
