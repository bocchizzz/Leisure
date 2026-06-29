package com.campus.market.notify.service;

import com.campus.market.notify.entity.AuditLog;
import com.campus.market.notify.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 审计日志服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    
    /**
     * 记录审计日志（内部接口）
     */
    @Transactional
    public void recordLog(Long userId, String username, String action, String method, 
                          String params, String ip, Long costTime, String status, String errorMsg) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setMethod(method);
        auditLog.setParams(params);
        auditLog.setIp(ip);
        auditLog.setCostTime(costTime);
        auditLog.setStatus(status);
        auditLog.setErrorMsg(errorMsg);
        
        auditLogRepository.save(auditLog);
    }
    
    /**
     * 分页查询审计日志（管理员）
     */
    public Page<AuditLog> listLogs(String action, String username, Pageable pageable) {
        if (action != null || username != null) {
            return auditLogRepository.findByFilters(action, username, pageable);
        }
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    /**
     * 获取用户操作日志
     */
    public Page<AuditLog> getUserLogs(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
}
