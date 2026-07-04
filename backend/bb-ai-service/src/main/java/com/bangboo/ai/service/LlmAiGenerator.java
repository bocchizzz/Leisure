package com.bangboo.ai.service;

import com.bangboo.ai.client.LlmClient;
import com.bangboo.ai.client.LlmGenerated;
import com.bangboo.ai.client.LlmResult;
import com.bangboo.ai.dto.BountySuggestionResult;
import com.bangboo.ai.dto.ChatResponse;
import com.bangboo.ai.dto.CourtRoastResult;
import com.bangboo.ai.dto.CourtSummaryResult;
import com.bangboo.ai.dto.RiskAssessmentResult;
import com.bangboo.ai.dto.SmartSearchResult;
import com.bangboo.ai.dto.TaskBreakdownResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于真实大模型（DeepSeek）的生成器。
 * 每个能力构造 system + user prompt 要求模型返回严格 JSON，解析为对应 VO。
 * 解析/调用失败向上抛异常，由 AiService 决定降级到规则生成。
 *
 * 提示词设计原则：
 * - system 明确角色、平台背景、字段填写标准、枚举约束、few-shot 示例；
 * - 强约束「只返回 JSON」，避免多余解释文本；
 * - 按能力分设温度：定价/风险/搜索等需稳定用低温；点评需活泼用高温。
 */
@Component
public class LlmAiGenerator {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 平台背景，注入到需要业务语境的提示词中。 */
    private static final String PLATFORM_BG =
            "「赏金布」是一个校园互助任务平台：委托人发布悬赏任务，猎人（学生）接单完成并获得积分赏金，"
            + "常见场景有跑腿代取、学业辅导、设计文案、活动帮手、维修等。赏金以平台积分计，"
            + "普通校园小任务通常在 5~50 积分区间，复杂或专业任务可更高。";

    private static final String CATEGORY_ENUM =
            "ERRAND(跑腿代取)/STUDY(学业辅导)/DESIGN(设计制作)/COPYWRITING(文案写作)/"
            + "REPAIR(维修调试)/ACTIVITY(活动帮手)/ONLINE(线上任务)/URGENT(紧急任务)/OTHER(其他)";
    private static final String DIFFICULTY_ENUM =
            "F(极易,几分钟)/E(简单,校内跑腿)/D(一般,需一定时间)/C(中等,需技能)/"
            + "B(较难,需专业能力)/A(困难,高要求)/S(极难,专家级)";

    private final LlmClient llmClient;

    public LlmAiGenerator(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    // ---------- 对话 ----------

    public LlmGenerated<ChatResponse> chat(String message, String context) {
        String sys = "你是校园赏金任务平台「赏金布」的智能助手「书记官」。" + PLATFORM_BG + "\n"
                + "你的职责：解答平台使用问题、引导用户发布任务、在用户描述了一个待办需求时建议其使用「任务拆解」功能。\n"
                + "回答要求：用简洁、友好、口语化的中文，控制在 2~4 句话；不要编造平台没有的功能。\n"
                + "当用户在描述一件想找人帮忙做的事（可拆解为任务）时，action 返回 \"SUGGEST_BREAKDOWN\"；"
                + "其余情况（闲聊、咨询规则等）返回 \"NONE\"。\n"
                + "必须严格只返回 JSON，不要任何多余文字：{\"reply\":\"回复文本\",\"action\":\"NONE 或 SUGGEST_BREAKDOWN\"}";
        String user = "用户消息：" + safe(message) + (isBlank(context) ? "" : ("\n对话上下文：" + context));
        LlmResult r = llmClient.complete(sys, user, true, 0.8);
        JsonNode n = readJson(r.content());
        ChatResponse vo = new ChatResponse(
                n.path("reply").asText(""),
                n.path("action").asText("NONE"),
                null
        );
        return new LlmGenerated<>(vo, r);
    }

    // ---------- 任务拆解 ----------

    public LlmGenerated<TaskBreakdownResult> breakdown(String rawText) {
        String sys = "你是「赏金布」平台的任务发布助手，擅长把用户零散的口语需求整理成规范的悬赏任务。" + PLATFORM_BG + "\n"
                + "请完成以下工作并输出结构化结果：\n"
                + "1) title：8~20 字、清晰吸引人的任务标题，突出要做什么；\n"
                + "2) category：从枚举中选最贴切的一个 —— " + CATEGORY_ENUM + "；\n"
                + "3) difficulty：客观评估难度，枚举 —— " + DIFFICULTY_ENUM + "；\n"
                + "4) suggestedBountyMin/Max：合理的积分赏金区间（整数，min<max），依据难度与工作量，"
                + "参考跑腿类约 5~15、设计文案类约 30~120、专业类可更高；\n"
                + "5) steps：3~6 条可执行的完成步骤，动词开头，具体不空泛；\n"
                + "6) riskTips：2~4 条对委托人/猎人的风险与注意事项（如财物交接、隐私、时间约定）。\n"
                + "示例输入：「帮我去菜鸟驿站取个快递送到8号宿舍楼，有点急」→ "
                + "{\"title\":\"急取快递送至8号宿舍楼\",\"category\":\"ERRAND\",\"difficulty\":\"E\","
                + "\"suggestedBountyMin\":8,\"suggestedBountyMax\":15,"
                + "\"steps\":[\"联系委托人确认取件码与地址\",\"前往菜鸟驿站凭码取件\",\"核对包裹完好\",\"按时送达8号宿舍楼并交接\"],"
                + "\"riskTips\":[\"当面清点包裹避免破损纠纷\",\"提前确认送达时间\"]}\n"
                + "必须严格只返回 JSON，字段与示例一致，不要任何多余文字。";
        String user = "用户原始需求：" + safe(rawText);
        LlmResult r = llmClient.complete(sys, user, true, 0.5);
        JsonNode n = readJson(r.content());
        TaskBreakdownResult vo = new TaskBreakdownResult(
                n.path("title").asText(""),
                normalizeCategory(n.path("category").asText("OTHER")),
                normalizeDifficulty(n.path("difficulty").asText("D")),
                n.path("suggestedBountyMin").asInt(10),
                n.path("suggestedBountyMax").asInt(30),
                toStringList(n.path("steps")),
                toStringList(n.path("riskTips"))
        );
        return new LlmGenerated<>(vo, r);
    }

    // ---------- 赏金建议 ----------

    public LlmGenerated<BountySuggestionResult> bountySuggestion(String category, String description, String rawText) {
        String sys = "你是「赏金布」平台的赏金定价顾问。" + PLATFORM_BG + "\n"
                + "请根据任务分类与描述，给出公平合理的积分赏金区间：\n"
                + "- 参考区间：跑腿代取 5~15、活动帮手 10~40、文案写作 20~80、设计制作 30~120、"
                + "学业辅导 30~100、维修调试 20~90，紧急或高要求任务可上浮 20%~50%；\n"
                + "- suggestedBountyMin < suggestedBountyMax，均为整数；\n"
                + "- reason：1~2 句说明定价依据（工作量、难度、时效）。\n"
                + "必须严格只返回 JSON：{\"suggestedBountyMin\":整数,\"suggestedBountyMax\":整数,\"reason\":\"定价理由\"}";
        String user = "任务分类：" + safe(category) + "\n任务描述：" + firstNonBlank(description, rawText);
        LlmResult r = llmClient.complete(sys, user, true, 0.3);
        JsonNode n = readJson(r.content());
        BountySuggestionResult vo = new BountySuggestionResult(
                n.path("suggestedBountyMin").asInt(10),
                n.path("suggestedBountyMax").asInt(30),
                n.path("reason").asText("")
        );
        return new LlmGenerated<>(vo, r);
    }

    // ---------- 风险评估 ----------

    public LlmGenerated<RiskAssessmentResult> riskAssessment(String description, String rawText) {
        String sys = "你是「赏金布」平台的任务风险评估专家。" + PLATFORM_BG + "\n"
                + "请识别该任务对委托人与猎人可能存在的风险，并给出可操作的规避建议：\n"
                + "- risks：2~5 条，覆盖财物安全、人身安全、隐私泄露、时间/质量纠纷、违规违法等维度中相关的；\n"
                + "- suggestions：2~5 条，与风险一一呼应的具体防范措施（如平台内交易、当面验收、留存凭证、事先约定）；\n"
                + "- 若任务涉嫌违规（代考、刷单、代写作业等），必须在 risks 中明确指出并在 suggestions 中提示谨慎/拒接。\n"
                + "必须严格只返回 JSON：{\"risks\":[\"风险1\",\"风险2\"],\"suggestions\":[\"建议1\",\"建议2\"]}";
        String user = "任务描述：" + firstNonBlank(description, rawText);
        LlmResult r = llmClient.complete(sys, user, true, 0.4);
        JsonNode n = readJson(r.content());
        RiskAssessmentResult vo = new RiskAssessmentResult(
                toStringList(n.path("risks")),
                toStringList(n.path("suggestions"))
        );
        return new LlmGenerated<>(vo, r);
    }

    // ---------- 智能搜索 ----------

    public LlmGenerated<SmartSearchResult> smartSearch(String rawText) {
        String sys = "你是「赏金布」平台的智能搜索助手，把用户的自然语言意图转成结构化搜索条件。" + PLATFORM_BG + "\n"
                + "- keyword：提炼 2~6 个字的核心搜索关键词（去掉「我想」「帮我」等语气词）；\n"
                + "- category：推断最可能的任务分类，枚举 —— " + CATEGORY_ENUM + "，无法判断填 OTHER；\n"
                + "- tips：1~3 条帮助用户更精准找到任务的搜索/筛选小建议。\n"
                + "示例：「想找个能帮忙做PPT的」→ {\"keyword\":\"PPT制作\",\"category\":\"DESIGN\",\"tips\":[\"可按赏金从低到高筛选\",\"注明交付时间更易匹配\"]}\n"
                + "必须严格只返回 JSON：{\"keyword\":\"关键词\",\"category\":\"枚举\",\"tips\":[\"提示1\"]}";
        String user = "用户搜索输入：" + safe(rawText);
        LlmResult r = llmClient.complete(sys, user, true, 0.4);
        JsonNode n = readJson(r.content());
        SmartSearchResult vo = new SmartSearchResult(
                n.path("keyword").asText(""),
                normalizeCategory(n.path("category").asText("OTHER")),
                toStringList(n.path("tips"))
        );
        return new LlmGenerated<>(vo, r);
    }

    // ---------- 案件摘要 ----------

    public LlmGenerated<CourtSummaryResult> courtSummary(Long caseId, String style, String caseContext) {
        String sys = "你是「赏金布·校园小法庭」的 AI 书记官，协助陪审团快速了解纠纷。" + PLATFORM_BG + "\n"
                + "小法庭用于处理任务履约纠纷，由委托人（原告或被告）与猎人对立陈述，陪审员投票裁决。\n"
                + "请基于【真实案件资料】客观中立地分析，严禁编造资料中不存在的事实：\n"
                + "- summary：150 字内概述纠纷起因、双方主张与争议核心；\n"
                + "- focusPoints：2~4 条争议焦点（陪审员应重点判断的问题）；\n"
                + "- evidenceAnalysis：结合已提交证据分析其证明力与不足；若无证据则说明「双方均未提交有效证据」；\n"
                + "- suggestion：给陪审团的初步倾向性参考（须注明「仅供参考，最终以投票/管理员裁决为准」）。\n"
                + "语气风格参考：" + safe(style) + "。必须严格只返回 JSON："
                + "{\"summary\":\"...\",\"focusPoints\":[\"...\"],\"evidenceAnalysis\":\"...\",\"suggestion\":\"...\"}";
        String user = isBlank(caseContext)
                ? ("案件ID：" + caseId + "（未获取到详细资料，请基于常见校园任务纠纷给出通用性分析，并在 summary 中说明资料缺失）")
                : ("以下是案件ID " + caseId + " 的真实资料：\n" + caseContext);
        LlmResult r = llmClient.complete(sys, user, true, 0.6);
        JsonNode n = readJson(r.content());
        CourtSummaryResult vo = new CourtSummaryResult(
                n.path("summary").asText(""),
                toStringList(n.path("focusPoints")),
                n.path("evidenceAnalysis").asText(""),
                n.path("suggestion").asText("")
        );
        return new LlmGenerated<>(vo, r);
    }

    // ---------- 案件点评 ----------

    public LlmGenerated<CourtRoastResult> courtRoast(Long caseId, String style, String caseContext) {
        String sys = "你是「赏金布·校园小法庭」的幽默书记官，用轻松诙谐、善意不刻薄的口吻点评这桩纠纷，"
                + "让围观的同学会心一笑，同时提醒双方以后如何避免类似矛盾。\n"
                + PLATFORM_BG + "\n"
                + "要求：基于【真实案件资料】展开，可适度调侃事件本身但绝不人身攻击、不辱骂、不涉及敏感话题；"
                + "80~200 字，接地气的校园口吻；结尾给一句正经的小提醒。\n"
                + "必须严格只返回 JSON：{\"roast\":\"点评文本\",\"style\":\"" + safe(style) + "\"}";
        String user = isBlank(caseContext)
                ? ("案件ID：" + caseId + "（无详细资料，请围绕常见校园任务纠纷做通用调侃）")
                : ("以下是案件ID " + caseId + " 的真实资料：\n" + caseContext);
        LlmResult r = llmClient.complete(sys, user, true, 1.0);
        JsonNode n = readJson(r.content());
        CourtRoastResult vo = new CourtRoastResult(
                n.path("roast").asText(""),
                n.path("style").asText(isBlank(style) ? "ROAST" : style)
        );
        return new LlmGenerated<>(vo, r);
    }

    // 说明：封面/头像为文生图，走独立的通义万相客户端（见 AiService / WanxImageClient），不走本类。

    // ---------- helpers ----------

    private JsonNode readJson(String content) {
        try {
            String cleaned = stripCodeFence(content);
            return MAPPER.readTree(cleaned);
        } catch (Exception ex) {
            throw new IllegalStateException("LLM 返回非合法 JSON: " + ex.getMessage(), ex);
        }
    }

    /** 去除模型可能包裹的 ```json ... ``` 代码围栏。 */
    private String stripCodeFence(String s) {
        if (s == null) {
            return "{}";
        }
        String t = s.trim();
        if (t.startsWith("```")) {
            int firstNl = t.indexOf('\n');
            if (firstNl > 0) {
                t = t.substring(firstNl + 1);
            }
            if (t.endsWith("```")) {
                t = t.substring(0, t.length() - 3);
            }
        }
        return t.trim();
    }

    private List<String> toStringList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node != null && node.isArray()) {
            node.forEach(item -> {
                String v = item.asText("");
                if (!v.isBlank()) {
                    list.add(v);
                }
            });
        }
        return list;
    }

    private String normalizeCategory(String c) {
        if (c == null) {
            return "OTHER";
        }
        String up = c.trim().toUpperCase();
        return switch (up) {
            case "ERRAND", "STUDY", "DESIGN", "COPYWRITING", "REPAIR", "ACTIVITY", "ONLINE", "URGENT", "OTHER" -> up;
            default -> "OTHER";
        };
    }

    private String normalizeDifficulty(String d) {
        if (d == null) {
            return "D";
        }
        String up = d.trim().toUpperCase();
        return List.of("F", "E", "D", "C", "B", "A", "S").contains(up) ? up : "D";
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        return b == null ? "" : b;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
