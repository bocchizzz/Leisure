package com.bangboo.adminops.service;

import com.bangboo.adminops.dto.OpsConfigVO;
import com.bangboo.adminops.entity.OpsConfigEntity;
import com.bangboo.adminops.repository.OpsConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpsConfigService {
    private static final Long DEFAULT_ID = 1L;

    private final OpsConfigRepository opsConfigRepository;

    public OpsConfigService(OpsConfigRepository opsConfigRepository) {
        this.opsConfigRepository = opsConfigRepository;
    }

    @Transactional(readOnly = true)
    public OpsConfigVO get() {
        return OpsConfigMapper.toVO(requireConfig());
    }

    @Transactional
    public OpsConfigVO update(OpsConfigVO request) {
        OpsConfigEntity config = requireConfig();
        config.setTaskReviewMode(request.taskReviewMode());
        config.setMinAutoPassSafetyScore(request.minAutoPassSafetyScore());
        config.setMaxAutoBlockSafetyScore(request.maxAutoBlockSafetyScore());
        config.setAiSafetyEnabled(request.aiSafetyEnabled());
        config.setAiOutputWatermark(request.aiOutputWatermark());
        config.setBannedKeywords(OpsConfigMapper.join(request.bannedKeywords()));
        config.setFileMaxSizeMb(request.fileMaxSizeMb());
        config.setAllowedFileTypes(OpsConfigMapper.join(request.allowedFileTypes()));
        config.setJuryMinReputation(request.juryMinReputation());
        config.setJuryMinCompletedTasks(request.juryMinCompletedTasks());
        config.setVoteQuorum(request.voteQuorum());
        config.setDisputeAutoEscalationHours(request.disputeAutoEscalationHours());
        return OpsConfigMapper.toVO(config);
    }

    private OpsConfigEntity requireConfig() {
        return opsConfigRepository.findById(DEFAULT_ID)
                .orElseGet(() -> opsConfigRepository.save(new OpsConfigEntity()));
    }
}
