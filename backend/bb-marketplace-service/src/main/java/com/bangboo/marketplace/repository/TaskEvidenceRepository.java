package com.bangboo.marketplace.repository;

import com.bangboo.marketplace.entity.TaskEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskEvidenceRepository extends JpaRepository<TaskEvidence, Long> {
    List<TaskEvidence> findByContractIdOrderByCreatedAtDesc(Long contractId);

    @Query("select coalesce(max(e.id), 0) from TaskEvidence e")
    long maxId();
}
