package com.bangboo.marketplace.repository;

import com.bangboo.marketplace.entity.TaskContract;
import com.bangboo.marketplace.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TaskContractRepository extends JpaRepository<TaskContract, Long> {
    Optional<TaskContract> findByTaskId(Long taskId);

    Page<TaskContract> findByPublisherId(Long publisherId, Pageable pageable);

    Page<TaskContract> findByPublisherIdAndStatus(Long publisherId, ContractStatus status, Pageable pageable);

    Page<TaskContract> findByHunterId(Long hunterId, Pageable pageable);

    Page<TaskContract> findByHunterIdAndStatus(Long hunterId, ContractStatus status, Pageable pageable);

    long countByStatus(ContractStatus status);

    @Query("select coalesce(max(c.id), 0) from TaskContract c")
    long maxId();
}
