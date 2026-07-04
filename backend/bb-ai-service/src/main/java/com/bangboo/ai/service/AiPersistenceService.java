package com.bangboo.ai.service;

import com.bangboo.ai.client.LlmResult;
import com.bangboo.ai.dto.CourtRoastResult;
import com.bangboo.ai.dto.CourtSummaryResult;
import com.bangboo.ai.dto.TaskBreakdownResult;
import com.bangboo.ai.entity.AiCallLog;
import com.bangboo.ai.entity.AiCourtSummary;
import com.bangboo.ai.entity.AiRoastComment;
import com.bangboo.ai.entity.AiTaskSuggestion;
import com.bangboo.ai.repository.AiCallLogRepository;
import com.bangboo.ai.repository.AiCourtSummaryRepository;
import com.bangboo.ai.repository.AiRoastCommentRepository;
import com.bangboo.ai.repository.AiTaskSuggestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * AI 结果与调用日志落表。全部 best-effort：落表失败不影响 AI 主功能返回。
 * 对齐需求文档 cq_ai_call_log / cq_ai_task_suggestion / cq_ai_court_summary / cq_ai_roast_comment。
 */
@Service
public class AiPersistenceService {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AiCallLogRepository callLogRepository;
    private final AiTaskSuggestionRepository suggestionRepository;
    private final AiCourtSummaryRepository summaryRepository;
    private final AiRoastCommentRepository roastRepository;
    private final IdGenerator idGenerator;

    public AiPersistenceService(
            AiCallLogRepository callLogRepository,
            AiTaskSuggestionRepository suggestionRepository,
            AiCourtSummaryRepository summaryRepository,
            AiRoastCommentRepository roastRepository,
            IdGenerator idGenerator
    ) {
        this.callLogRepository = callLogRepository;
        this.suggestionRepository = suggestionRepository;
        this.summaryRepository = summaryRepository;
        this.roastRepository = roastRepository;
        this.idGenerator = idGenerator;
    }

    /** 记录调用日志。status：SUCCESS / FAIL / TIMEOUT / DEGRADED。 */
    @Transactional
    public void logCall(Long userId, String businessType, String relatedType, Long relatedId,
                        String modelName, String promptVersion, LlmResult meta,
                        String status, String errorMessage) {
        try {
            AiCallLog log = new AiCallLog();
            log.setId(idGenerator.nextCallLogId());
            log.setUserId(userId);
            log.setBusinessType(businessType);
            log.setRelatedType(relatedType);
            log.setRelatedId(relatedId);
            log.setModelName(modelName);
            log.setPromptVersion(promptVersion);
            if (meta != null) {
                log.setRequestTokens(meta.requestTokens());
                log.setResponseTokens(meta.responseTokens());
                log.setLatencyMs(meta.latencyMs());
            }
            log.setStatus(status);
            log.setErrorMessage(truncate(errorMessage, 1000));
            log.setCreatedAt(Instant.now());
            callLogRepository.save(log);
        } catch (RuntimeException ignored) {
            // 日志落表失败不影响主功能。
        }
    }

    @Transactional
    public void saveTaskSuggestion(Long userId, String inputText, TaskBreakdownResult vo, String modelName) {
        try {
            AiTaskSuggestion s = new AiTaskSuggestion();
            s.setId(idGenerator.nextSuggestionId());
            s.setUserId(userId);
            s.setInputText(truncate(inputText, 60000));
            s.setSuggestedTitle(truncate(vo.title(), 100));
            s.setSuggestedCategory(vo.category());
            s.setSuggestedDifficulty(vo.difficulty());
            s.setSuggestedBountyMin(vo.suggestedBountyMin());
            s.setSuggestedBountyMax(vo.suggestedBountyMax());
            s.setTaskSteps(vo.steps() == null ? null : String.join("\n", vo.steps()));
            s.setRiskTips(vo.riskTips() == null ? null : String.join("\n", vo.riskTips()));
            s.setAiResultJson(toJson(vo));
            s.setModelName(modelName);
            s.setCreatedAt(Instant.now());
            suggestionRepository.save(s);
        } catch (RuntimeException ignored) {
        }
    }

    @Transactional
    public void saveCourtSummary(Long caseId, Long createdBy, CourtSummaryResult vo, String modelName, String promptVersion) {
        try {
            AiCourtSummary s = new AiCourtSummary();
            s.setId(idGenerator.nextSummaryId());
            s.setCaseId(caseId == null ? 0L : caseId);
            s.setSummaryText(vo.summary());
            s.setDisputeFocus(vo.focusPoints() == null ? null : String.join("\n", vo.focusPoints()));
            s.setEvidenceAnalysis(vo.evidenceAnalysis());
            s.setSuggestion(vo.suggestion());
            s.setModelName(modelName);
            s.setPromptVersion(promptVersion);
            s.setCreatedAt(Instant.now());
            s.setCreatedBy(createdBy);
            summaryRepository.save(s);
        } catch (RuntimeException ignored) {
        }
    }

    @Transactional
    public void saveRoast(Long caseId, CourtRoastResult vo, String modelName) {
        try {
            AiRoastComment c = new AiRoastComment();
            c.setId(idGenerator.nextRoastId());
            c.setCaseId(caseId == null ? 0L : caseId);
            c.setStyle(vo.style() == null ? "ROAST" : vo.style());
            c.setCommentText(vo.roast());
            c.setSafetyStatus("PASS");
            c.setModelName(modelName);
            c.setCreatedAt(Instant.now());
            roastRepository.save(c);
        } catch (RuntimeException ignored) {
        }
    }

    public long callCount() {
        return callLogRepository.count();
    }

    private String toJson(Object o) {
        try {
            return MAPPER.writeValueAsString(o);
        } catch (Exception e) {
            return null;
        }
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() > max ? s.substring(0, max) : s;
    }
}
