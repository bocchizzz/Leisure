package com.bangboo.reputation.dto.internal;

/** marketplace 契约评价标记请求。评价成功后调用 review-flags 传入。 */
public record ReviewFlagRequest(
        Long reviewerId,
        String role,
        Long reviewId
) {
}
