package com.bangboo.iam.dto;

import com.bangboo.iam.enums.CertificationStatus;

import java.time.Instant;

public record CertificationVO(
        Long id,
        Long userId,
        String realName,
        String school,
        String studentNo,
        String materialUrl,
        CertificationStatus status,
        Long reviewerId,
        String reviewComment,
        Instant createdAt,
        Instant reviewedAt
) {
}
