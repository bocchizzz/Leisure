package com.bangboo.ai.service;

import com.bangboo.ai.dto.BangbooAvatarRequest;
import com.bangboo.ai.dto.BangbooAvatarResult;
import com.bangboo.ai.dto.BountySuggestionResult;
import com.bangboo.ai.dto.ChatResponse;
import com.bangboo.ai.dto.CourtRoastResult;
import com.bangboo.ai.dto.CourtSummaryResult;
import com.bangboo.ai.dto.RiskAssessmentResult;
import com.bangboo.ai.dto.SmartSearchResult;
import com.bangboo.ai.dto.TaskBreakdownResult;
import com.bangboo.ai.dto.TaskCoverImageResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 规则/模板生成引擎（第一版不接真实大模型）。
 * 依据关键词推断任务分类、难度、赏金区间，并生成结构化结果，字段严格对齐前端 ai.ts。
 * 生成图片返回可被浏览器访问的占位 URL。
 */
@Component
public class AiGenerator {

    /** 分类关键词映射（对齐前端 TaskCategory / TaskCategoryName）。 */
    private static final Map<String, List<String>> CATEGORY_KEYWORDS = new LinkedHashMap<>();

    static {
        CATEGORY_KEYWORDS.put("ERRAND", List.of("跑腿", "代拿", "代取", "取快递", "带饭", "带", "送", "取", "买", "代买", "排队"));
        CATEGORY_KEYWORDS.put("STUDY", List.of("辅导", "作业", "学习", "答疑", "补课", "讲题", "考试", "复习", "论文"));
        CATEGORY_KEYWORDS.put("DESIGN", List.of("设计", "海报", "logo", "ps", "绘画", "插画", "封面", "美工", "排版设计"));
        CATEGORY_KEYWORDS.put("COPYWRITING", List.of("文案", "写作", "写", "推文", "稿", "策划案", "翻译"));
        CATEGORY_KEYWORDS.put("REPAIR", List.of("维修", "修", "电脑", "手机", "网络", "装机", "重装", "故障"));
        CATEGORY_KEYWORDS.put("ACTIVITY", List.of("活动", "协助", "帮忙", "布置", "搬", "志愿", "摆摊", "签到"));
        CATEGORY_KEYWORDS.put("ONLINE", List.of("线上", "远程", "录入", "整理", "excel", "表格", "剪辑", "视频"));
        CATEGORY_KEYWORDS.put("URGENT", List.of("急", "紧急", "马上", "立刻", "尽快", "今晚", "现在"));
    }

    private static final Map<String, String> CATEGORY_NAME = Map.of(
            "ERRAND", "跑腿代办", "STUDY", "学习辅导", "DESIGN", "设计创作",
            "COPYWRITING", "文案写作", "REPAIR", "维修技术", "ACTIVITY", "活动协助",
            "ONLINE", "线上协作", "URGENT", "紧急救场", "OTHER", "其他"
    );

    // ---------- 对话 ----------

    public ChatResponse chat(String message, String context) {
        String text = safe(message).trim();
        if (text.isEmpty()) {
            return new ChatResponse("你好，我是赏金布的智能书记官，可以帮你拆解任务、建议赏金、评估风险。请描述你的需求。", "NONE", null);
        }
        String category = inferCategory(text);
        String reply = "我理解你想发布一个「" + CATEGORY_NAME.getOrDefault(category, "其他")
                + "」类的任务。建议把完成标准和截止时间写清楚，这样更容易招募到合适的猎人。"
                + "如果需要，我可以帮你自动拆解任务或建议赏金区间。";
        return new ChatResponse(reply, "SUGGEST_BREAKDOWN", Map.of("category", category));
    }

    // ---------- 任务拆解 ----------

    public TaskBreakdownResult breakdown(String rawText) {
        String text = safe(rawText).trim();
        String category = inferCategory(text);
        String difficulty = inferDifficulty(text, category);
        int[] bounty = bountyRange(category, difficulty);
        String title = buildTitle(text, category);

        List<String> steps = new ArrayList<>();
        steps.add("确认需求细节与完成标准");
        steps.add("与委托人沟通时间与交付方式");
        steps.add(executeStep(category));
        steps.add("提交履约证据并等待验收");

        List<String> riskTips = new ArrayList<>();
        riskTips.add("明确完成标准，避免验收争议");
        if ("URGENT".equals(category)) {
            riskTips.add("时间紧张，接单前确认自己能按时完成");
        }
        if ("REPAIR".equals(category) || "DESIGN".equals(category)) {
            riskTips.add("保留过程记录与原始文件，便于举证");
        }
        riskTips.add("涉及财物请当面清点交接");

        return new TaskBreakdownResult(title, category, difficulty, bounty[0], bounty[1], steps, riskTips);
    }

    // ---------- 赏金建议 ----------

    public BountySuggestionResult bountySuggestion(String category, String description, String rawText) {
        String text = firstNonBlank(rawText, description);
        String cat = (category != null && !category.isBlank()) ? category.toUpperCase() : inferCategory(text);
        String difficulty = inferDifficulty(text, cat);
        int[] bounty = bountyRange(cat, difficulty);
        String reason = "根据任务分类「" + CATEGORY_NAME.getOrDefault(cat, "其他")
                + "」与预估难度 " + difficulty + " 级，建议赏金区间 "
                + bounty[0] + "~" + bounty[1] + " 积分。可结合紧急程度与工作量微调。";
        return new BountySuggestionResult(bounty[0], bounty[1], reason);
    }

    // ---------- 风险评估 ----------

    public RiskAssessmentResult riskAssessment(String description, String rawText) {
        String text = firstNonBlank(description, rawText);
        String category = inferCategory(text);
        List<String> risks = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        risks.add("需求描述不够具体，可能导致完成标准分歧");
        suggestions.add("补充明确的完成标准与验收方式");

        if ("URGENT".equals(category)) {
            risks.add("时间要求紧，存在无法按时完成的风险");
            suggestions.add("设置合理的截止时间并预留缓冲");
        }
        if (containsAny(text, List.of("钱", "现金", "支付", "转账", "代付"))) {
            risks.add("涉及资金往来，存在纠纷或诈骗风险");
            suggestions.add("优先使用平台内约定，当面交接并保留凭证");
        }
        if ("REPAIR".equals(category)) {
            risks.add("维修类任务可能造成设备二次损坏");
            suggestions.add("维修前拍照记录设备状态，明确责任边界");
        }
        if (safe(text).length() < 10) {
            risks.add("信息过少，猎人难以评估工作量");
            suggestions.add("补充任务背景、地点、时间等关键信息");
        }
        suggestions.add("完成后及时提交履约证据，便于确认");
        return new RiskAssessmentResult(risks, suggestions);
    }

    // ---------- 任务封面 ----------

    public TaskCoverImageResult coverImage(String title, String description, String category) {
        String cat = (category != null && !category.isBlank()) ? category.toUpperCase() : inferCategory(firstNonBlank(title, description));
        String prompt = "赏金布任务封面，主题：" + firstNonBlank(title, CATEGORY_NAME.getOrDefault(cat, "校园任务"))
                + "，风格：扁平插画、暖色调、校园元素";
        String imageUrl = "/uploads/ai/cover/" + cat.toLowerCase() + "-" + slug(title) + ".svg";
        return new TaskCoverImageResult(imageUrl, prompt, "rule-template");
    }

    // ---------- 邦布头像 ----------

    public BangbooAvatarResult bangbooAvatar(BangbooAvatarRequest request) {
        String mascot = (request.mascotKey() != null && !request.mascotKey().isBlank())
                ? request.mascotKey() : "kacha";
        String seed = (request.seed() != null && !request.seed().isBlank())
                ? request.seed() : Integer.toHexString((mascot + safe(request.referenceImageUrl())).hashCode());
        String prompt = "邦布风格头像，吉祥物：" + mascot + "，可爱、圆润、科技感";
        String imageUrl = "/uploads/ai/avatar/" + mascot + "-" + seed + ".svg";
        return new BangbooAvatarResult(imageUrl, prompt, "rule-template", mascot);
    }

    // ---------- 智能搜索 ----------

    public SmartSearchResult smartSearch(String rawText) {
        String text = safe(rawText).trim();
        String category = inferCategory(text);
        String keyword = buildKeyword(text, category);
        List<String> tips = new ArrayList<>();
        tips.add("已为你匹配「" + CATEGORY_NAME.getOrDefault(category, "其他") + "」类任务");
        tips.add("可尝试关键词：" + keyword);
        if (!"OTHER".equals(category)) {
            tips.add("也可在筛选中选择分类「" + CATEGORY_NAME.get(category) + "」");
        }
        return new SmartSearchResult(keyword, category, tips);
    }

    // ---------- 案件摘要 ----------

    public CourtSummaryResult courtSummary(Long caseId, String style) {
        String summary = "本案围绕契约履约结果产生争议。委托人认为交付不符合约定标准，猎人主张已按要求完成。"
                + "双方均提交了相关证据，需结合完成标准与证据链综合判断。";
        List<String> focusPoints = List.of(
                "契约约定的完成标准是否清晰",
                "猎人提交的履约证据是否充分",
                "交付结果与约定的偏差程度",
                "沟通记录是否显示存在变更"
        );
        String evidenceAnalysis = "现有证据基本可还原履约过程，但对交付质量的判断存在主观空间，建议以约定标准为准绳。";
        String suggestion = "建议根据实际履约比例部分支持猎人，鼓励双方就差异部分协商。";
        return new CourtSummaryResult(summary, focusPoints, evidenceAnalysis, suggestion);
    }

    // ---------- 案件点评 ----------

    public CourtRoastResult courtRoast(Long caseId, String style) {
        String roast = "两位这场拉锯战，一个说「我尽力了」，一个说「这也叫完成？」——"
                + "下次记得把完成标准写清楚，证据留全，邦布小法庭也就不用加班了。";
        return new CourtRoastResult(roast, style == null || style.isBlank() ? "ROAST" : style);
    }

    // ---------- 规则推断辅助 ----------

    private String inferCategory(String text) {
        String t = safe(text).toLowerCase();
        if (t.isEmpty()) {
            return "OTHER";
        }
        String best = "OTHER";
        int bestHits = 0;
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            int hits = 0;
            for (String kw : entry.getValue()) {
                if (t.contains(kw.toLowerCase())) {
                    hits++;
                }
            }
            if (hits > bestHits) {
                bestHits = hits;
                best = entry.getKey();
            }
        }
        return best;
    }

    private String inferDifficulty(String text, String category) {
        String t = safe(text);
        int len = t.length();
        boolean hard = containsAny(t, List.of("专业", "复杂", "高质量", "精通", "定制", "系统", "开发", "算法"));
        boolean easy = containsAny(t, List.of("简单", "顺手", "随便", "帮个忙", "小事"));

        // 基于分类的基线难度
        String base = switch (category) {
            case "ERRAND", "URGENT" -> "E";
            case "ACTIVITY", "ONLINE" -> "D";
            case "STUDY", "COPYWRITING" -> "C";
            case "DESIGN", "REPAIR" -> "B";
            default -> "D";
        };
        if (easy) {
            return "F";
        }
        if (hard) {
            return upgrade(base);
        }
        if (len > 120) {
            return upgrade(base);
        }
        return base;
    }

    private String upgrade(String difficulty) {
        List<String> order = List.of("F", "E", "D", "C", "B", "A", "S");
        int idx = order.indexOf(difficulty);
        if (idx < 0) {
            return "C";
        }
        return order.get(Math.min(idx + 1, order.size() - 1));
    }

    /** 分类+难度 -> 赏金区间（积分）。 */
    private int[] bountyRange(String category, String difficulty) {
        int base = switch (category) {
            case "ERRAND" -> 10;
            case "URGENT" -> 25;
            case "ACTIVITY" -> 20;
            case "ONLINE" -> 30;
            case "STUDY" -> 40;
            case "COPYWRITING" -> 45;
            case "DESIGN" -> 60;
            case "REPAIR" -> 50;
            default -> 20;
        };
        double factor = switch (difficulty) {
            case "F" -> 0.6;
            case "E" -> 0.8;
            case "D" -> 1.0;
            case "C" -> 1.4;
            case "B" -> 1.8;
            case "A" -> 2.4;
            case "S" -> 3.2;
            default -> 1.0;
        };
        int min = (int) Math.round(base * factor);
        int max = (int) Math.round(base * factor * 1.8);
        if (min < 5) {
            min = 5;
        }
        if (max <= min) {
            max = min + 10;
        }
        return new int[]{min, max};
    }

    private String buildTitle(String text, String category) {
        String t = safe(text).trim();
        if (t.isEmpty()) {
            return CATEGORY_NAME.getOrDefault(category, "校园任务") + "求助";
        }
        String firstLine = t.split("[\\n。.!！?？]")[0].trim();
        if (firstLine.length() > 20) {
            firstLine = firstLine.substring(0, 20);
        }
        return firstLine.isEmpty() ? CATEGORY_NAME.getOrDefault(category, "校园任务") : firstLine;
    }

    private String buildKeyword(String text, String category) {
        String t = safe(text).trim();
        if (t.isEmpty()) {
            return CATEGORY_NAME.getOrDefault(category, "任务");
        }
        String kw = t.split("[\\s，,。.!！?？]")[0].trim();
        if (kw.length() > 12) {
            kw = kw.substring(0, 12);
        }
        return kw.isEmpty() ? CATEGORY_NAME.getOrDefault(category, "任务") : kw;
    }

    private String executeStep(String category) {
        return switch (category) {
            case "ERRAND" -> "按约定路线取送物品";
            case "STUDY" -> "按约定时间进行辅导讲解";
            case "DESIGN" -> "完成设计稿并交付源文件";
            case "COPYWRITING" -> "撰写并交付文案初稿";
            case "REPAIR" -> "排查并修复问题";
            case "ACTIVITY" -> "到场协助完成活动事项";
            case "ONLINE" -> "线上完成并交付成果";
            case "URGENT" -> "第一时间响应并处理";
            default -> "按约定完成任务主体";
        };
    }

    private static boolean containsAny(String text, List<String> keywords) {
        String t = safe(text).toLowerCase();
        for (String kw : keywords) {
            if (t.contains(kw.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private static String slug(String s) {
        if (s == null || s.isBlank()) {
            return "task";
        }
        String cleaned = s.replaceAll("[^a-zA-Z0-9]", "");
        if (cleaned.isEmpty()) {
            return Integer.toHexString(s.hashCode());
        }
        return cleaned.length() > 16 ? cleaned.substring(0, 16).toLowerCase() : cleaned.toLowerCase();
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        return b == null ? "" : b;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
