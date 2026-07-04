package com.bangboo.ai.client;

import com.bangboo.ai.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Optional;

/**
 * 调用小法庭内部接口拉取真实案件资料，拼装为供大模型使用的中文案情文本。
 * court 未启动或调用失败时返回 Optional.empty()，由上层降级（prompt 走"资料缺失"兜底）。
 * court 返回统一 ApiResponse 包裹 {code,message,data}，此处解包 data 节点。
 */
@Component
public class CourtInternalClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final WebClient webClient;
    private final AiProperties properties;

    public CourtInternalClient(WebClient webClient, AiProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    /** 拉取案件资料并拼装为案情文本；失败返回 empty。 */
    public Optional<String> fetchCaseContext(Long caseId) {
        if (caseId == null) {
            return Optional.empty();
        }
        try {
            String raw = webClient.get()
                    .uri(properties.getCourtBaseUrl() + "/internal/court-cases/" + caseId)
                    .header("X-Internal-Token", properties.getInternalToken())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();
            JsonNode data = MAPPER.readTree(raw == null ? "{}" : raw).path("data");
            if (data.isMissingNode() || data.isNull()) {
                return Optional.empty();
            }
            String text = buildCaseText(data);
            return text.isBlank() ? Optional.empty() : Optional.of(text);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    /** 把 CourtCaseDetailVO 的 JSON 拼装成结构化中文案情文本。 */
    private String buildCaseText(JsonNode data) {
        JsonNode c = data.path("courtCase");
        StringBuilder sb = new StringBuilder();

        sb.append("【案件基本信息】\n");
        appendLine(sb, "案件标题", c.path("caseTitle").asText(""));
        appendLine(sb, "关联任务", c.path("taskTitle").asText(""));
        appendLine(sb, "纠纷类型", c.path("type").asText(""));
        appendLine(sb, "案件状态", c.path("status").asText(""));
        appendLine(sb, "原告(委托方)", c.path("plaintiffName").asText(""));
        appendLine(sb, "被告(猎人方)", c.path("defendantName").asText(""));
        appendLine(sb, "初始陈述", c.path("initialStatement").asText(""));

        JsonNode statements = data.path("statements");
        if (statements.isArray() && statements.size() > 0) {
            sb.append("\n【双方陈述】\n");
            int i = 1;
            for (JsonNode s : statements) {
                String who = s.path("userName").asText("某方");
                String role = s.path("role").asText("");
                String content = s.path("content").asText("");
                if (!content.isBlank()) {
                    sb.append(i++).append(". ").append(who)
                            .append(role.isBlank() ? "" : ("(" + role + ")"))
                            .append("：").append(content).append("\n");
                }
            }
        }

        JsonNode evidences = data.path("evidences");
        if (evidences.isArray() && evidences.size() > 0) {
            sb.append("\n【已提交证据】\n");
            int i = 1;
            for (JsonNode e : evidences) {
                String who = e.path("submitterName").asText("某方");
                String type = e.path("type").asText("");
                String content = e.path("content").asText("");
                String fileUrl = e.path("fileUrl").asText("");
                sb.append(i++).append(". 提交人：").append(who);
                if (!type.isBlank()) {
                    sb.append("，类型：").append(type);
                }
                if (!content.isBlank()) {
                    sb.append("，说明：").append(content);
                }
                if (!fileUrl.isBlank()) {
                    sb.append("，附件：").append(fileUrl);
                }
                sb.append("\n");
            }
        } else {
            sb.append("\n【已提交证据】\n（双方暂未提交证据）\n");
        }

        JsonNode ruling = data.path("ruling");
        if (ruling.isObject() && !ruling.isEmpty()) {
            sb.append("\n【裁决结果】\n");
            appendLine(sb, "裁决", ruling.path("result").asText(""));
            appendLine(sb, "理由", ruling.path("reason").asText(""));
        }

        return sb.toString().trim();
    }

    private void appendLine(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            sb.append(label).append("：").append(value).append("\n");
        }
    }
}
