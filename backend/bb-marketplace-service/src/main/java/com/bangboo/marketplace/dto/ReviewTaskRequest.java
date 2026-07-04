package com.bangboo.marketplace.dto;

/** 管理员审核任务请求，前端 body 字段为 approved / comment。 */
public record ReviewTaskRequest(Boolean approved, String comment) {
}
