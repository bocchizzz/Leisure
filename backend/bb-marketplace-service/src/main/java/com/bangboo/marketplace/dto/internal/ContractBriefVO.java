package com.bangboo.marketplace.dto.internal;

/** 契约简要信息，供 court / reputation 调用。字段对齐 A 侧 court ContractBriefVO 与 mock-b-services。 */
public record ContractBriefVO(
        Long id,
        Long taskId,
        String taskTitle,
        Long publisherId,
        String publisherName,
        Long hunterId,
        String hunterName,
        String status,
        Double bountyAmount,
        String bountyType
) {
}
