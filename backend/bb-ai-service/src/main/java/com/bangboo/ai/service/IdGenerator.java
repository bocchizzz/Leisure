package com.bangboo.ai.service;

import com.bangboo.ai.repository.AiCallLogRepository;
import com.bangboo.ai.repository.AiCourtSummaryRepository;
import com.bangboo.ai.repository.AiRoastCommentRepository;
import com.bangboo.ai.repository.AiTaskSuggestionRepository;
import org.springframework.stereotype.Component;

/** 应用层主键分配（与其他 B 侧服务一致）。 */
@Component
public class IdGenerator {
    private static final long BASE = 1L;

    private final AiCallLogRepository callLogRepository;
    private final AiTaskSuggestionRepository suggestionRepository;
    private final AiCourtSummaryRepository summaryRepository;
    private final AiRoastCommentRepository roastRepository;

    public IdGenerator(
            AiCallLogRepository callLogRepository,
            AiTaskSuggestionRepository suggestionRepository,
            AiCourtSummaryRepository summaryRepository,
            AiRoastCommentRepository roastRepository
    ) {
        this.callLogRepository = callLogRepository;
        this.suggestionRepository = suggestionRepository;
        this.summaryRepository = summaryRepository;
        this.roastRepository = roastRepository;
    }

    public synchronized Long nextCallLogId() {
        return Math.max(callLogRepository.maxId(), BASE) + 1;
    }

    public synchronized Long nextSuggestionId() {
        return Math.max(suggestionRepository.maxId(), BASE) + 1;
    }

    public synchronized Long nextSummaryId() {
        return Math.max(summaryRepository.maxId(), BASE) + 1;
    }

    public synchronized Long nextRoastId() {
        return Math.max(roastRepository.maxId(), BASE) + 1;
    }
}
