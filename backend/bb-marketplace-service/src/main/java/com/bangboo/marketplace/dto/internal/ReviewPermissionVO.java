package com.bangboo.marketplace.dto.internal;

/** 契约评价权限，供 reputation 调用。字段对齐 mock-b-services review-permission 返回。 */
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
