package com.bangboo.marketplace.dto;

import java.time.Instant;

/** 提交接单申请请求，对齐前端 CreateApplicationRequest。 */
public record CreateApplicationRequest(
        String applyMessage,
        Instant expectedFinishTime
) {
}
