package com.bangboo.marketplace.dto;

import com.bangboo.marketplace.enums.EvidenceType;

/** 提交履约证据请求，对齐前端 CreateEvidenceRequest。fileUrl 与 content 至少一个非空。 */
public record CreateEvidenceRequest(
        EvidenceType type,
        String fileUrl,
        String content
) {
}
