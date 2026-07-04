package com.bangboo.ai.client;

import com.bangboo.ai.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek（OpenAI 兼容）大模型调用客户端。
 * 调用 /chat/completions，强制 JSON 输出，返回文本与 token 用量。
 * 任何异常向上抛出，由上层决定降级到规则生成。
 */
@Component
public class LlmClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final WebClient webClient;
    private final AiProperties properties;

    public LlmClient(WebClient webClient, AiProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    public boolean enabled() {
        return properties.isLlmEnabled();
    }

    /**
     * 发起一次对话补全（默认温度 0.7）。
     * @param systemPrompt 系统提示（设定角色与输出格式约束）
     * @param userPrompt   用户输入
     * @param jsonMode     是否要求返回 JSON 对象
     */
    public LlmResult complete(String systemPrompt, String userPrompt, boolean jsonMode) {
        return complete(systemPrompt, userPrompt, jsonMode, 0.7);
    }

    /**
     * 发起一次对话补全，可指定采样温度。
     * @param temperature 采样温度：定价/风险等需稳定输出用低温(0.2~0.4)，点评等需活泼用高温(0.9~1.1)
     */
    public LlmResult complete(String systemPrompt, String userPrompt, boolean jsonMode, double temperature) {
        AiProperties.Llm llm = properties.getLlm();
        long start = System.currentTimeMillis();

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("model", llm.getModel());
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt == null ? "" : systemPrompt),
                Map.of("role", "user", "content", userPrompt == null ? "" : userPrompt)
        ));
        body.put("temperature", temperature);
        body.put("stream", false);
        if (jsonMode) {
            body.put("response_format", Map.of("type", "json_object"));
        }

        String raw = webClient.post()
                .uri(llm.getBaseUrl() + "/chat/completions")
                .header("Authorization", "Bearer " + llm.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(Math.max(5, llm.getTimeoutSeconds())))
                .block();

        int latency = (int) (System.currentTimeMillis() - start);
        return parse(raw, latency);
    }

    private LlmResult parse(String raw, int latency) {
        try {
            JsonNode root = MAPPER.readTree(raw);
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            JsonNode usage = root.path("usage");
            Integer promptTokens = usage.has("prompt_tokens") ? usage.get("prompt_tokens").asInt() : null;
            Integer completionTokens = usage.has("completion_tokens") ? usage.get("completion_tokens").asInt() : null;
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("LLM 返回内容为空");
            }
            return new LlmResult(content, promptTokens, completionTokens, latency);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("解析 LLM 响应失败: " + ex.getMessage(), ex);
        }
    }
}
