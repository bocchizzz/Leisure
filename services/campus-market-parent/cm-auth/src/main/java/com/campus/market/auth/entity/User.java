package com.campus.market.auth.entity;

import com.campus.market.auth.enums.UserRole;
import com.campus.market.auth.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体类
 */
@Entity
@Table(name = "sys_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(name = "password_hash", nullable = false, length = 100)
    private String password;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "sys_user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<UserRole> roles = new HashSet<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
    
    @Column(nullable = false)
    private Integer reputation = 100;
    
    @Column(length = 100)
    private String email;
    
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean hasRole(UserRole role) {
        return roles != null && roles.contains(role);
    }
    
    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }
    
    public void increaseReputation(int points) {
        this.reputation = Math.min(this.reputation + points, 200);
    }
    
    public void decreaseReputation(int points) {
        this.reputation = Math.max(this.reputation - points, 0);
    }
    
    public String getReputationLevel() {
        if (reputation >= 180) return "钻石";
        if (reputation >= 150) return "铂金";
        if (reputation >= 120) return "黄金";
        if (reputation >= 90) return "白银";
        if (reputation >= 60) return "青铜";
        return "警告";
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        if (roles == null) {
            roles = new HashSet<>();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
