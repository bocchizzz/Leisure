package com.bangboo.marketplace.service;

import com.bangboo.marketplace.repository.TaskApplicationRepository;
import com.bangboo.marketplace.repository.TaskContractRepository;
import com.bangboo.marketplace.repository.TaskEvidenceRepository;
import com.bangboo.marketplace.repository.TaskFavoriteRepository;
import com.bangboo.marketplace.repository.TaskHistoryRepository;
import com.bangboo.marketplace.repository.TaskRepository;
import org.springframework.stereotype.Component;

/**
 * 应用层主键分配（与 A 侧 court-service 风格一致）。
 * 使用 max(id)+1，并对每类实体设定起始基线，避开 DataInitializer 种子固定 ID 段，
 * 保证种子（task 101/201、application 601、contract 301/302 等）不与新建业务数据冲突。
 */
@Component
public class IdGenerator {
    private static final long TASK_BASE = 1000L;
    private static final long APPLICATION_BASE = 700L;
    private static final long CONTRACT_BASE = 400L;
    private static final long EVIDENCE_BASE = 1000L;
    private static final long FAVORITE_BASE = 1L;
    private static final long HISTORY_BASE = 1L;

    private final TaskRepository taskRepository;
    private final TaskApplicationRepository applicationRepository;
    private final TaskContractRepository contractRepository;
    private final TaskEvidenceRepository evidenceRepository;
    private final TaskFavoriteRepository favoriteRepository;
    private final TaskHistoryRepository historyRepository;

    public IdGenerator(
            TaskRepository taskRepository,
            TaskApplicationRepository applicationRepository,
            TaskContractRepository contractRepository,
            TaskEvidenceRepository evidenceRepository,
            TaskFavoriteRepository favoriteRepository,
            TaskHistoryRepository historyRepository
    ) {
        this.taskRepository = taskRepository;
        this.applicationRepository = applicationRepository;
        this.contractRepository = contractRepository;
        this.evidenceRepository = evidenceRepository;
        this.favoriteRepository = favoriteRepository;
        this.historyRepository = historyRepository;
    }

    public synchronized Long nextTaskId() {
        return Math.max(taskRepository.maxId(), TASK_BASE) + 1;
    }

    public synchronized Long nextApplicationId() {
        return Math.max(applicationRepository.maxId(), APPLICATION_BASE) + 1;
    }

    public synchronized Long nextContractId() {
        return Math.max(contractRepository.maxId(), CONTRACT_BASE) + 1;
    }

    public synchronized Long nextEvidenceId() {
        return Math.max(evidenceRepository.maxId(), EVIDENCE_BASE) + 1;
    }

    public synchronized Long nextFavoriteId() {
        return Math.max(favoriteRepository.maxId(), FAVORITE_BASE) + 1;
    }

    public synchronized Long nextHistoryId() {
        return Math.max(historyRepository.maxId(), HISTORY_BASE) + 1;
    }
}
