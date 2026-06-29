package com.campus.market.auth.repository;

import com.campus.market.auth.entity.User;
import com.campus.market.auth.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓库
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR u.username LIKE %:keyword% OR u.email LIKE %:keyword%) " +
           "AND (:status IS NULL OR u.status = :status)")
    Page<User> findByKeywordAndStatus(@Param("keyword") String keyword, 
                                       @Param("status") UserStatus status, 
                                       Pageable pageable);
    
    List<User> findByIdIn(List<Long> ids);
    
    long countByStatus(UserStatus status);
}
