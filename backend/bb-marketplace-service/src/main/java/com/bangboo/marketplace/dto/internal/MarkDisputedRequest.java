package com.bangboo.marketplace.dto.internal;

/** 契约纠纷标记请求，court 调用 mark-disputed 时传入。 */
public record MarkDisputedRequest(Long caseId, Long operatorId) {
}
