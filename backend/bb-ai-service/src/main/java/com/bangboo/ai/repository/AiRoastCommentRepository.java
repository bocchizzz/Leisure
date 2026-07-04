package com.bangboo.ai.repository;

import com.bangboo.ai.entity.AiRoastComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AiRoastCommentRepository extends JpaRepository<AiRoastComment, Long> {
    @Query("select coalesce(max(r.id), 0) from AiRoastComment r")
    long maxId();
}
