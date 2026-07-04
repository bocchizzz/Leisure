package com.bangboo.iam.repository;

import com.bangboo.iam.entity.Certification;
import com.bangboo.iam.enums.CertificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    boolean existsByUserIdAndStatus(Long userId, CertificationStatus status);

    Optional<Certification> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Certification> findByStatus(CertificationStatus status, Pageable pageable);
}
