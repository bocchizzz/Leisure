package com.bangboo.ai.client;

/**
 * LLM 调用结果封装。
 * content：模型返回的文本（要求为 JSON 字符串）；
 * requestTokens/responseTokens：token 用量；latencyMs：耗时。
 */
public record LlmResult(
        String content,
        Integer requestTokens,
        Integer responseTokens,
        Integer latencyMs
) {
}
