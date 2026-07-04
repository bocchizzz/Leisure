package com.bangboo.reputation.dto.internal;

/**
 * marketplace 契约评价权限返回。字段对齐 marketplace ReviewPermissionVO。
 * 用于评价前判定当前用户是否可评、评价方向、被评价人。
 */
public record ReviewPermissionVO(
        Long contractId,
        Long taskId,
        String taskTitle,
        Long publisherId,
        Long hunterId,
        String status,
        Long reviewerId,
        Long revieweeId,
        String role,
        boolean allowed
) {
}
