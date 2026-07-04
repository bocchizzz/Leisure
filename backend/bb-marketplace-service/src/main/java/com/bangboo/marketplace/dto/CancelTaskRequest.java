package com.bangboo.marketplace.dto;

/** 取消任务请求，前端 body 字段为 reason。 */
public record CancelTaskRequest(String reason) {
}
