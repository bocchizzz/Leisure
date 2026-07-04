package com.bangboo.ai.service;

import com.bangboo.ai.client.CourtInternalClient;
import com.bangboo.ai.client.LlmClient;
import com.bangboo.ai.client.LlmGenerated;
import com.bangboo.ai.client.WanxImageClient;
import com.bangboo.ai.config.AiProperties;
import com.bangboo.ai.dto.BangbooAvatarRequest;
import com.bangboo.ai.dto.BangbooAvatarResult;
import com.bangboo.ai.dto.BountySuggestionRequest;
import com.bangboo.ai.dto.BountySuggestionResult;
import com.bangboo.ai.dto.ChatRequest;
import com.bangboo.ai.dto.ChatResponse;
import com.bangboo.ai.dto.CourtAiRequest;
import com.bangboo.ai.dto.CourtRoastResult;
import com.bangboo.ai.dto.CourtSummaryResult;
import com.bangboo.ai.dto.RiskAssessmentRequest;
import com.bangboo.ai.dto.RiskAssessmentResult;
import com.bangboo.ai.dto.SmartSearchResult;
import com.bangboo.ai.dto.TaskBreakdownResult;
import com.bangboo.ai.dto.TaskCoverImageRequest;
import com.bangboo.ai.dto.TaskCoverImageResult;
import com.bangboo.common.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * AI 能力编排：登录校验 -> 生成（优先真实模型，失败回退规则）-> 落表（结果 + 调用日志）。
 * provider=rule（默认）时直接用规则生成；provider=llm 且配置了 apiKey 时优先用 DeepSeek，
 * 调用/解析失败自动降级到规则生成，并记 status=DEGRADED，保证前端结构永远稳定。
 * /ai/* 需要登录（分工 9.10）。
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final AiGenerator ruleGenerator;
    private final LlmAiGenerator llmGenerator;
    private final LlmClient llmClient;
    private final WanxImageClient wanxClient;
    private final CourtInternalClient courtClient;
    private final AiAuthService authService;
    private final AiPersistenceService persistence;
    private final AiProperties properties;

    public AiService(
            AiGenerator ruleGenerator,
            LlmAiGenerator llmGenerator,
            LlmClient llmClient,
            WanxImageClient wanxClient,
            CourtInternalClient courtClient,
            AiAuthService authService,
            AiPersistenceService persistence,
            AiProperties properties
    ) {
        this.ruleGenerator = ruleGenerator;
        this.llmGenerator = llmGenerator;
        this.llmClient = llmClient;
        this.wanxClient = wanxClient;
        this.courtClient = courtClient;
        this.authService = authService;
        this.persistence = persistence;
        this.properties = properties;
    }

    private boolean llmOn() {
        return properties.isLlmEnabled() && llmClient.enabled();
    }

    private String model() {
        return properties.getLlm().getModel();
    }

    private String promptVersion() {
        return properties.getLlm().getPromptVersion();
    }

    // ---------- 对话 ----------

    public ChatResponse chat(ChatRequest request) {
        Long uid = authService.requireUser().uid();
        String message = request == null ? null : request.message();
        String context = request == null ? null : request.context();
        if (llmOn()) {
            try {
                LlmGenerated<ChatResponse> g = llmGenerator.chat(message, context);
                persistence.logCall(uid, "CHAT", null, null, model(), promptVersion(), g.meta(), "SUCCESS", null);
                return g.result();
            } catch (RuntimeException ex) {
                persistence.logCall(uid, "CHAT", null, null, model(), promptVersion(), null, "DEGRADED", ex.getMessage());
            }
        } else {
            persistence.logCall(uid, "CHAT", null, null, "rule", "rule", null, "SUCCESS", null);
        }
        return ruleGenerator.chat(message, context);
    }

    // ---------- 任务拆解 ----------

    public TaskBreakdownResult breakdown(String rawText) {
        Long uid = authService.requireUser().uid();
        if (llmOn()) {
            try {
                LlmGenerated<TaskBreakdownResult> g = llmGenerator.breakdown(rawText);
                persistence.logCall(uid, "TASK_SUGGESTION", "TASK", null, model(), promptVersion(), g.meta(), "SUCCESS", null);
                persistence.saveTaskSuggestion(uid, rawText, g.result(), model());
                return g.result();
            } catch (RuntimeException ex) {
                persistence.logCall(uid, "TASK_SUGGESTION", "TASK", null, model(), promptVersion(), null, "DEGRADED", ex.getMessage());
            }
        } else {
            persistence.logCall(uid, "TASK_SUGGESTION", "TASK", null, "rule", "rule", null, "SUCCESS", null);
        }
        TaskBreakdownResult vo = ruleGenerator.breakdown(rawText);
        persistence.saveTaskSuggestion(uid, rawText, vo, "rule");
        return vo;
    }

    // ---------- 赏金建议 ----------

    public BountySuggestionResult bountySuggestion(BountySuggestionRequest request) {
        Long uid = authService.requireUser().uid();
        String category = request == null ? null : request.category();
        String description = request == null ? null : request.description();
        String rawText = request == null ? null : request.rawText();
        if (llmOn()) {
            try {
                LlmGenerated<BountySuggestionResult> g = llmGenerator.bountySuggestion(category, description, rawText);
                persistence.logCall(uid, "BOUNTY", "TASK", null, model(), promptVersion(), g.meta(), "SUCCESS", null);
                return g.result();
            } catch (RuntimeException ex) {
                persistence.logCall(uid, "BOUNTY", "TASK", null, model(), promptVersion(), null, "DEGRADED", ex.getMessage());
            }
        } else {
            persistence.logCall(uid, "BOUNTY", "TASK", null, "rule", "rule", null, "SUCCESS", null);
        }
        return ruleGenerator.bountySuggestion(category, description, rawText);
    }

    // ---------- 风险评估 ----------

    public RiskAssessmentResult riskAssessment(RiskAssessmentRequest request) {
        Long uid = authService.requireUser().uid();
        String description = request == null ? null : request.description();
        String rawText = request == null ? null : request.rawText();
        if (llmOn()) {
            try {
                LlmGenerated<RiskAssessmentResult> g = llmGenerator.riskAssessment(description, rawText);
                persistence.logCall(uid, "RISK_CHECK", "TASK", null, model(), promptVersion(), g.meta(), "SUCCESS", null);
                return g.result();
            } catch (RuntimeException ex) {
                persistence.logCall(uid, "RISK_CHECK", "TASK", null, model(), promptVersion(), null, "DEGRADED", ex.getMessage());
            }
        } else {
            persistence.logCall(uid, "RISK_CHECK", "TASK", null, "rule", "rule", null, "SUCCESS", null);
        }
        return ruleGenerator.riskAssessment(description, rawText);
    }

    // ---------- 任务封面（文生图：纯文字 → 通义万相，失败降级占位图） ----------

    public TaskCoverImageResult coverImage(TaskCoverImageRequest request) {
        Long uid = authService.requireUser().uid();
        String title = request == null ? null : request.title();
        String description = request == null ? null : request.description();
        String category = request == null ? null : request.category();
        String wanxModel = properties.getWanx().getModel();
        if (wanxClient.enabled()) {
            String prompt = WanxPrompts.coverPrompt(title, description, "校园任务", category);
            try {
                String localUrl = wanxClient.generate(prompt, null, "cover");
                persistence.logCall(uid, "COVER", "TASK", null, wanxModel, "wanx", null, "SUCCESS", null);
                return new TaskCoverImageResult(localUrl, prompt, "wanx");
            } catch (RuntimeException ex) {
                log.error("通义万相封面生成失败，降级为占位图: {}", ex.getMessage());
                persistence.logCall(uid, "COVER", "TASK", null, wanxModel, "wanx", null, "DEGRADED", ex.getMessage());
            }
        } else {
            persistence.logCall(uid, "COVER", "TASK", null, "rule", "rule", null, "SUCCESS", null);
        }
        return ruleGenerator.coverImage(title, description, category);
    }

    // ---------- 邦布头像（图生图：参考图 + 文字 → 通义万相，失败降级占位图） ----------

    public BangbooAvatarResult bangbooAvatar(BangbooAvatarRequest request) {
        Long uid = authService.requireUser().uid();
        BangbooAvatarRequest req = request == null ? new BangbooAvatarRequest(null, null, null) : request;
        String mascot = (req.mascotKey() != null && !req.mascotKey().isBlank()) ? req.mascotKey() : "kacha";
        String wanxModel = properties.getWanx().getModel();
        if (wanxClient.enabled()) {
            String prompt = WanxPrompts.avatarPrompt(req);
            byte[] refBytes = wanxClient.fetchReferenceImageBytes(req.referenceImageUrl());
            try {
                String localUrl = wanxClient.generate(prompt, refBytes, "avatar");
                persistence.logCall(uid, "AVATAR", "USER", uid, wanxModel, "wanx", null, "SUCCESS", null);
                return new BangbooAvatarResult(localUrl, prompt, "wanx", mascot);
            } catch (RuntimeException ex) {
                log.error("通义万相头像生成失败，降级为占位图: {}", ex.getMessage());
                persistence.logCall(uid, "AVATAR", "USER", uid, wanxModel, "wanx", null, "DEGRADED", ex.getMessage());
            }
        } else {
            persistence.logCall(uid, "AVATAR", "USER", uid, "rule", "rule", null, "SUCCESS", null);
        }
        return ruleGenerator.bangbooAvatar(req);
    }

    // ---------- 智能搜索 ----------

    public SmartSearchResult smartSearch(String rawText) {
        Long uid = authService.requireUser().uid();
        if (llmOn()) {
            try {
                LlmGenerated<SmartSearchResult> g = llmGenerator.smartSearch(rawText);
                persistence.logCall(uid, "SMART_SEARCH", null, null, model(), promptVersion(), g.meta(), "SUCCESS", null);
                return g.result();
            } catch (RuntimeException ex) {
                persistence.logCall(uid, "SMART_SEARCH", null, null, model(), promptVersion(), null, "DEGRADED", ex.getMessage());
            }
        } else {
            persistence.logCall(uid, "SMART_SEARCH", null, null, "rule", "rule", null, "SUCCESS", null);
        }
        return ruleGenerator.smartSearch(rawText);
    }

    // ---------- 案件摘要 ----------

    public CourtSummaryResult courtSummary(CourtAiRequest request) {
        Long uid = authService.requireUser().uid();
        Long caseId = request == null ? null : request.caseId();
        String style = request == null ? null : request.style();
        if (llmOn()) {
            try {
                String caseContext = courtClient.fetchCaseContext(caseId).orElse(null);
                LlmGenerated<CourtSummaryResult> g = llmGenerator.courtSummary(caseId, style, caseContext);
                persistence.logCall(uid, "COURT_SUMMARY", "COURT", caseId, model(), promptVersion(), g.meta(), "SUCCESS", null);
                persistence.saveCourtSummary(caseId, uid, g.result(), model(), promptVersion());
                return g.result();
            } catch (RuntimeException ex) {
                persistence.logCall(uid, "COURT_SUMMARY", "COURT", caseId, model(), promptVersion(), null, "DEGRADED", ex.getMessage());
            }
        } else {
            persistence.logCall(uid, "COURT_SUMMARY", "COURT", caseId, "rule", "rule", null, "SUCCESS", null);
        }
        CourtSummaryResult vo = ruleGenerator.courtSummary(caseId, style);
        persistence.saveCourtSummary(caseId, uid, vo, "rule", "rule");
        return vo;
    }

    // ---------- 案件点评 ----------

    public CourtRoastResult courtRoast(CourtAiRequest request) {
        Long uid = authService.requireUser().uid();
        Long caseId = request == null ? null : request.caseId();
        String style = request == null ? null : request.style();
        if (llmOn()) {
            try {
                String caseContext = courtClient.fetchCaseContext(caseId).orElse(null);
                LlmGenerated<CourtRoastResult> g = llmGenerator.courtRoast(caseId, style, caseContext);
                persistence.logCall(uid, "ROAST_COMMENT", "COURT", caseId, model(), promptVersion(), g.meta(), "SUCCESS", null);
                persistence.saveRoast(caseId, g.result(), model());
                return g.result();
            } catch (RuntimeException ex) {
                persistence.logCall(uid, "ROAST_COMMENT", "COURT", caseId, model(), promptVersion(), null, "DEGRADED", ex.getMessage());
            }
        } else {
            persistence.logCall(uid, "ROAST_COMMENT", "COURT", caseId, "rule", "rule", null, "SUCCESS", null);
        }
        CourtRoastResult vo = ruleGenerator.courtRoast(caseId, style);
        persistence.saveRoast(caseId, vo, "rule");
        return vo;
    }

    public CurrentUser currentUser() {
        return authService.requireUser();
    }
}
