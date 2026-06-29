package com.campus.market.common.feign;

import com.campus.market.common.exception.BusinessException;
import com.campus.market.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Feign错误解码器：解析ApiResponse，解析失败必须回退
 * 关键：防止"解析地狱"导致调用方崩溃
 */
@Component
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ErrorDecoder defaultDecoder = new Default();
    
    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String rawBody = null;
        
        try {
            // 1. 读取响应体
            if (response.body() != null) {
                try (InputStream inputStream = response.body().asInputStream()) {
                    byte[] bytes = inputStream.readAllBytes();
                    rawBody = new String(bytes, StandardCharsets.UTF_8);
                }
            }
            
            // 2. 检查是否为JSON格式
            if (rawBody != null && rawBody.trim().startsWith("{")) {
                try {
                    ApiResponse<?> apiResponse = objectMapper.readValue(rawBody, ApiResponse.class);
                    if (apiResponse != null && apiResponse.getMessage() != null) {
                        // 成功解析ApiResponse，抛出业务异常
                        log.warn("Feign call failed [{}]: code={}, message={}", 
                                methodKey, apiResponse.getCode(), apiResponse.getMessage());
                        return new BusinessException(apiResponse.getCode(), apiResponse.getMessage());
                    }
                } catch (Exception parseEx) {
                    log.warn("Failed to parse ApiResponse for [{}], fallback to generic error. Raw: {}", 
                            methodKey, truncate(rawBody, 200));
                }
            }
            
            // 3. 无法解析为ApiResponse，使用状态码映射
            String message = mapStatusToMessage(status, rawBody);
            log.warn("Feign call failed [{}]: status={}, message={}", methodKey, status, message);
            
            // 4. 对于可重试的错误（503/504），返回RetryableException
            if (status == 503 || status == 504) {
                return new BusinessException(status, message);
            }
            
            return new BusinessException(status, message);
            
        } catch (IOException e) {
            log.error("Error reading response body for [{}]", methodKey, e);
            return new BusinessException(503, "上游服务响应读取失败");
        } catch (Exception e) {
            log.error("Unexpected error decoding response for [{}]", methodKey, e);
            return new BusinessException(500, "服务调用异常");
        }
    }
    
    private String mapStatusToMessage(int status, String rawBody) {
        return switch (status) {
            case 400 -> "请求参数错误";
            case 401 -> "认证失败";
            case 403 -> "权限不足";
            case 404 -> "资源不存在";
            case 409 -> "资源冲突";
            case 429 -> "请求过于频繁";
            case 500 -> "上游服务内部错误";
            case 502 -> "网关错误";
            case 503 -> "上游服务不可用";
            case 504 -> "上游服务超时";
            default -> "服务调用失败(HTTP " + status + ")";
        };
    }
    
    private String truncate(String str, int maxLen) {
        if (str == null) return null;
        return str.length() <= maxLen ? str : str.substring(0, maxLen) + "...";
    }
}
