package com.bangboo.marketplace.dto;

/** 取消契约请求，前端 body 字段为 cancelReason。 */
public record CancelContractRequest(String cancelReason) {
}
