package com.bangboo.adminops.service;

import com.bangboo.adminops.dto.AuditLogVO;
import com.bangboo.adminops.dto.CreateAuditLogRequest;
import com.bangboo.adminops.entity.AuditLog;
import com.bangboo.adminops.repository.AuditLogRepository;
import com.bangboo.common.response.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<AuditLogVO> list(int page, int size) {
        Page<AuditLog> result = auditLogRepository.findAll(
                PageRequest.of(Math.max(page, 0), normalizeSize(size), Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return new PageResult<>(
                result.getContent().stream().map(AuditLogMapper::toVO).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize(),
                result.isFirst(),
                result.isLast()
        );
    }

    @Transactional
    public AuditLogVO create(CreateAuditLogRequest request) {
        AuditLog log = new AuditLog();
        log.setOperatorId(request.operatorId());
        log.setOperatorName(trimToNull(request.operatorName()));
        log.setAction(request.action().trim());
        log.setTargetType(trimToNull(request.targetType()));
        log.setTargetId(request.targetId());
        log.setDetail(trimToNull(request.detail()));
        return AuditLogMapper.toVO(auditLogRepository.save(log));
    }

    private static int normalizeSize(int size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
