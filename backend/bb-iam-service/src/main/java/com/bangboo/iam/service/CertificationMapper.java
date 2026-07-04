package com.bangboo.iam.service;

import com.bangboo.iam.dto.CertificationVO;
import com.bangboo.iam.entity.Certification;

public final class CertificationMapper {
    private CertificationMapper() {
    }

    public static CertificationVO toVO(Certification certification) {
        return new CertificationVO(
                certification.getId(),
                certification.getUserId(),
                certification.getRealName(),
                certification.getSchool(),
                certification.getStudentNo(),
                certification.getMaterialUrl(),
                certification.getStatus(),
                certification.getReviewerId(),
                certification.getReviewComment(),
                certification.getCreatedAt(),
                certification.getReviewedAt()
        );
    }
}
