package com.bangboo.reputation.repository;

import com.bangboo.reputation.entity.Review;
import com.bangboo.reputation.enums.ReviewRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);

    List<Review> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    List<Review> findByContractId(Long contractId);

    Optional<Review> findByContractIdAndRole(Long contractId, ReviewRole role);

    boolean existsByContractIdAndRole(Long contractId, ReviewRole role);

    @Query("select coalesce(max(r.id), 0) from Review r")
    long maxId();
}
