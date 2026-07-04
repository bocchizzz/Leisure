package com.bangboo.adminops.repository;

import com.bangboo.adminops.entity.OpsConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpsConfigRepository extends JpaRepository<OpsConfigEntity, Long> {
}
