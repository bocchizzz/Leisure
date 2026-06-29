package com.campus.market.notify.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审计日志实体
 */
@Entity
@Table(name = "sys_audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(length = 50)
    private String username;
    
    @Column(nullable = false, length = 100)
    private String action;
    
    @Column(length = 200)
    private String method;
    
    @Column(columnDefinition = "TEXT")
    private String params;
    
    @Column(length = 50)
    private String ip;
    
    @Column(name = "cost_time")
    private Long costTime;
    
    @Column(length = 20)
    private String status;
    
    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
