package com.bangboo.marketplace.dto;

import java.time.Instant;

/** 修改申请请求，对齐前端 UpdateApplicationRequest。 */
public record UpdateApplicationRequest(
        String applyMessage,
        Instant expectedFinishTime
) {
}
