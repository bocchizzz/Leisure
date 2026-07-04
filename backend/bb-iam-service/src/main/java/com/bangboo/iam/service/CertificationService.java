package com.bangboo.iam.service;

import com.bangboo.common.constant.ErrorCode;
import com.bangboo.common.exception.BusinessException;
import com.bangboo.common.exception.ResourceNotFoundException;
import com.bangboo.common.response.PageResult;
import com.bangboo.iam.dto.CertificationVO;
import com.bangboo.iam.dto.CreateCertificationRequest;
import com.bangboo.iam.dto.ReviewCertificationRequest;
import com.bangboo.iam.entity.Certification;
import com.bangboo.iam.entity.IamUser;
import com.bangboo.iam.enums.CertificationStatus;
import com.bangboo.iam.repository.CertificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class CertificationService {
    private final CertificationRepository certificationRepository;
    private final UserService userService;

    public CertificationService(CertificationRepository certificationRepository, UserService userService) {
        this.certificationRepository = certificationRepository;
        this.userService = userService;
    }

    @Transactional
    public CertificationVO submit(Long userId, CreateCertificationRequest request) {
        if (certificationRepository.existsByUserIdAndStatus(userId, CertificationStatus.PENDING)) {
            throw new BusinessException(ErrorCode.CONFLICT, "已有待审核认证");
        }
        Certification certification = new Certification();
        certification.setUserId(userId);
        certification.setRealName(request.realName().trim());
        certification.setSchool(request.school().trim());
        certification.setStudentNo(request.studentNo().trim());
        certification.setMaterialUrl(request.materialUrl().trim());
        certification.setStatus(CertificationStatus.PENDING);
        return CertificationMapper.toVO(certificationRepository.save(certification));
    }

    @Transactional(readOnly = true)
    public CertificationVO latestMine(Long userId) {
        Optional<Certification> certification = certificationRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);
        return certification.map(CertificationMapper::toVO).orElse(null);
    }

    @Transactional(readOnly = true)
    public PageResult<CertificationVO> adminList(int page, int size, String status) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizeSize(size));
        Page<Certification> result;
        if (status != null && !status.isBlank()) {
            result = certificationRepository.findByStatus(CertificationStatus.valueOf(status.trim()), pageable);
        } else {
            result = certificationRepository.findAll(pageable);
        }
        return new PageResult<>(
                result.getContent().stream().map(CertificationMapper::toVO).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize(),
                result.isFirst(),
                result.isLast()
        );
    }

    @Transactional
    public CertificationVO review(Long id, Long reviewerId, ReviewCertificationRequest request) {
        Certification certification = certificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("认证记录不存在"));
        certification.setStatus(request.approved() ? CertificationStatus.APPROVED : CertificationStatus.REJECTED);
        certification.setReviewerId(reviewerId);
        certification.setReviewComment(request.comment());
        certification.setReviewedAt(Instant.now());

        IamUser user = userService.requireUserById(certification.getUserId());
        user.setCampusVerified(request.approved());
        if (request.approved()) {
            user.setSchool(certification.getSchool());
        }
        return CertificationMapper.toVO(certification);
    }

    private static int normalizeSize(int size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }
}
