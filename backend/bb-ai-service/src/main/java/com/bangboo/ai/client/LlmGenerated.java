package com.bangboo.ai.client;

/**
 * LLM 生成结果包装：解析后的业务对象 + 本次调用的元数据（token/耗时）。
 * @param <T> 业务结果类型
 */
public record LlmGenerated<T>(
        T result,
        LlmResult meta
) {
}
