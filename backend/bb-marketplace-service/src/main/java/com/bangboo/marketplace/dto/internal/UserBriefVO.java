package com.bangboo.marketplace.dto.internal;

import java.util.List;

/** IAM 用户简要信息，用于保存快照。对齐 A 侧 /internal/users/{id}/brief。 */
public record UserBriefVO(
        Long id,
        String username,
        String nickname,
        String avatarUrl,
        String status,
        Boolean campusVerified,
        Integer reputation,
        Integer hunterLevel,
        String hunterTitle,
        List<String> roles
) {
}
