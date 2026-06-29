package com.campus.market.notify.service;

import com.campus.market.common.exception.BusinessException;
import com.campus.market.common.exception.ResourceNotFoundException;
import com.campus.market.notify.entity.Appeal;
import com.campus.market.notify.enums.AppealStatus;
import com.campus.market.notify.enums.MessageType;
import com.campus.market.notify.repository.AppealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 申诉服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppealService {
    
    private final AppealRepository appealRepository;
    private final MessageService messageService;
    
    /**
     * 提交申诉
     */
    @Transactional
    public Appeal submitAppeal(Long userId, String type, String content, String contact) {
        Appeal appeal = new Appeal();
        appeal.setUserId(userId);
        appeal.setType(type);
        appeal.setContent(content);
        appeal.setContact(contact);
        appeal.setStatus(AppealStatus.PENDING);
        
        appeal = appealRepository.save(appeal);
        log.info("Appeal submitted: userId={}, type={}", userId, type);
        
        return appeal;
    }
    
    /**
     * 获取用户申诉列表
     */
    public Page<Appeal> getUserAppeals(Long userId, Pageable pageable) {
        return appealRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * 获取申诉详情
     */
    public Appeal getAppealById(Long appealId, Long userId) {
        return appealRepository.findByIdAndUserId(appealId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Appeal", appealId));
    }
    
    // ========== 管理员接口 ==========
    
    /**
     * 分页查询申诉（管理员）
     */
    public Page<Appeal> listAppeals(AppealStatus status, Pageable pageable) {
        if (status != null) {
            return appealRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }
        return appealRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    /**
     * 处理申诉（管理员）
     */
    @Transactional
    public Appeal processAppeal(Long appealId, AppealStatus newStatus, String response) {
        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new ResourceNotFoundException("Appeal", appealId));
        
        if (appeal.getStatus() == AppealStatus.RESOLVED || appeal.getStatus() == AppealStatus.REJECTED) {
            throw new BusinessException("申诉已处理完毕");
        }
        
        appeal.setStatus(newStatus);
        appeal.setResponse(response);
        appeal = appealRepository.save(appeal);
        
        // 发送通知给用户
        String title = newStatus == AppealStatus.RESOLVED ? "申诉已通过" : 
                       newStatus == AppealStatus.REJECTED ? "申诉已拒绝" : "申诉处理中";
        String content = response != null ? response : "您的申诉已更新，请查看详情。";
        messageService.sendMessage(appeal.getUserId(), MessageType.APPEAL, title, content, appealId);
        
        log.info("Appeal processed: appealId={}, status={}", appealId, newStatus);
        return appeal;
    }
    
    /**
     * 获取待处理申诉数
     */
    public long getPendingCount() {
        return appealRepository.countByStatus(AppealStatus.PENDING);
    }
}
