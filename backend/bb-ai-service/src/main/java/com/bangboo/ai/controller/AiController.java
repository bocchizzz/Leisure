package com.bangboo.ai.controller;

import com.bangboo.ai.dto.BangbooAvatarRequest;
import com.bangboo.ai.dto.BangbooAvatarResult;
import com.bangboo.ai.dto.BountySuggestionRequest;
import com.bangboo.ai.dto.BountySuggestionResult;
import com.bangboo.ai.dto.BreakdownRequest;
import com.bangboo.ai.dto.ChatRequest;
import com.bangboo.ai.dto.ChatResponse;
import com.bangboo.ai.dto.CourtAiRequest;
import com.bangboo.ai.dto.CourtRoastResult;
import com.bangboo.ai.dto.CourtSummaryResult;
import com.bangboo.ai.dto.RiskAssessmentRequest;
import com.bangboo.ai.dto.RiskAssessmentResult;
import com.bangboo.ai.dto.SmartSearchRequest;
import com.bangboo.ai.dto.SmartSearchResult;
import com.bangboo.ai.dto.TaskBreakdownResult;
import com.bangboo.ai.dto.TaskCoverImageRequest;
import com.bangboo.ai.dto.TaskCoverImageResult;
import com.bangboo.ai.service.AiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 书记官对外接口。对齐前端 ai.ts。
 * 路径不含 /api 前缀，由 Gateway 转发。/ai/* 需要登录。
 */
@RestController
@RequestMapping("/ai")
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody(required = false) ChatRequest request) {
        return aiService.chat(request);
    }

    @PostMapping("/tasks/breakdown")
    public TaskBreakdownResult breakdown(@RequestBody(required = false) BreakdownRequest request) {
        return aiService.breakdown(request == null ? null : request.rawText());
    }

    @PostMapping("/tasks/bounty-suggestion")
    public BountySuggestionResult bountySuggestion(@RequestBody(required = false) BountySuggestionRequest request) {
        return aiService.bountySuggestion(request);
    }

    @PostMapping("/tasks/risk-assessment")
    public RiskAssessmentResult riskAssessment(@RequestBody(required = false) RiskAssessmentRequest request) {
        return aiService.riskAssessment(request);
    }

    @PostMapping("/tasks/cover-image")
    public TaskCoverImageResult coverImage(@RequestBody(required = false) TaskCoverImageRequest request) {
        return aiService.coverImage(request);
    }

    @PostMapping("/profile/bangboo-avatar")
    public BangbooAvatarResult bangbooAvatar(@RequestBody(required = false) BangbooAvatarRequest request) {
        return aiService.bangbooAvatar(request);
    }

    @PostMapping("/tasks/smart-search")
    public SmartSearchResult smartSearch(@RequestBody(required = false) SmartSearchRequest request) {
        return aiService.smartSearch(request == null ? null : request.rawText());
    }

    @PostMapping("/court/summary")
    public CourtSummaryResult courtSummary(@RequestBody(required = false) CourtAiRequest request) {
        return aiService.courtSummary(request);
    }

    @PostMapping("/court/roast")
    public CourtRoastResult courtRoast(@RequestBody(required = false) CourtAiRequest request) {
        return aiService.courtRoast(request);
    }
}
