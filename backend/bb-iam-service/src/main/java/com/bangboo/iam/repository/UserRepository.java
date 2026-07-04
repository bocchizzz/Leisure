package com.bangboo.iam.repository;

import com.bangboo.iam.entity.IamUser;
import com.bangboo.iam.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<IamUser, Long> {
    Optional<IamUser> findByUsername(String username);

    boolean existsByUsername(String username);

    long countByCampusVerifiedTrue();

    Page<IamUser> findByStatus(UserStatus status, Pageable pageable);
}
