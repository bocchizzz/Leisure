package com.bangboo.reputation.client;

import com.bangboo.common.exception.BusinessException;
import com.bangboo.reputation.config.ReputationProperties;
import com.bangboo.reputation.dto.internal.CreateMessageRequest;
import com.bangboo.reputation.dto.internal.ReputationAdjustmentResultVO;
import com.bangboo.reputation.dto.internal.ReviewFlagRequest;
import com.bangboo.reputation.dto.internal.ReviewPermissionVO;
import com.bangboo.reputation.dto.internal.UserBriefVO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * 服务间内部调用客户端。
 * - reviewPermission：评价前必须成功的权限校验（分工 13.2），不可用时抛 503 阻断主流程。
 * - markReviewFlag / createMessage：best-effort，失败不回滚主业务。
 * - adjustIamReputation：更新 IAM 当前信誉分，best-effort，返回前后分值供记流水。
 * - userBrief：读接口，失败降级 Optional.empty。
 */
@Component
public class InternalApiClient {
    private final WebClient webClient;
    private final ReputationProperties properties;

    public InternalApiClient(WebClient webClient, ReputationProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    /** 评价前校验契约评价权限（必须成功，失败阻断）。 */
    public ReviewPermissionVO reviewPermission(Long contractId, Long userId) {
        String url = properties.getMarketplaceBaseUrl()
                + "/internal/contracts/" + contractId + "/review-permission?userId=" + userId;
        return getRequired(url, ReviewPermissionVO.class, "评价服务暂时不可用，请稍后再试");
    }

    /** 评价成功后标记契约评价状态（best-effort）。 */
    public void markReviewFlag(Long contractId, ReviewFlagRequest request) {
        postBestEffort(properties.getMarketplaceBaseUrl()
                + "/internal/contracts/" + contractId + "/review-flags", request);
    }

    /** 更新 IAM 当前信誉分，返回前后分值（best-effort）。 */
    public Optional<ReputationAdjustmentResultVO> adjustIamReputation(
            Long userId, int delta, String sourceType, Long sourceId, String reason) {
        String url = properties.getIamBaseUrl() + "/internal/users/" + userId + "/reputation-adjustments";
        Map<String, Object> body = Map.of(
                "delta", delta,
                "sourceType", sourceType == null ? "" : sourceType,
                "sourceId", sourceId == null ? 0 : sourceId,
                "reason", reason == null ? "" : reason
        );
        return postOptional(url, body, ReputationAdjustmentResultVO.class);
    }

    /** 用户简要信息，用于补昵称头像快照（失败降级）。 */
    public Optional<UserBriefVO> userBrief(Long userId) {
        return getOptional(properties.getIamBaseUrl() + "/internal/users/" + userId + "/brief", UserBriefVO.class);
    }

    /** 通知被评价人（best-effort）。 */
    public void createMessage(CreateMessageRequest request) {
        postBestEffort(properties.getMessageBaseUrl() + "/internal/messages", request);
    }

    // ---------- low-level ----------

    private <T> T getRequired(String url, Class<T> type, String unavailableMessage) {
        try {
            T result = webClient.get()
                    .uri(url)
                    .header("X-Internal-Token", properties.getInternalToken())
                    .retrieve()
                    .bodyToMono(ApiResponseWrapper.class)
                    .map(wrapper -> wrapper.to(type))
                    .timeout(Duration.ofSeconds(3))
                    .block();
            if (result == null) {
                throw new BusinessException(503, unavailableMessage);
            }
            return result;
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new BusinessException(503, unavailableMessage);
        }
    }

    private <T> Optional<T> getOptional(String url, Class<T> type) {
        try {
            T result = webClient.get()
                    .uri(url)
                    .header("X-Internal-Token", properties.getInternalToken())
                    .retrieve()
                    .bodyToMono(ApiResponseWrapper.class)
                    .map(wrapper -> wrapper.to(type))
                    .timeout(Duration.ofSeconds(2))
                    .onErrorResume(ex -> reactor.core.publisher.Mono.empty())
                    .block();
            return Optional.ofNullable(result);
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private <T> Optional<T> postOptional(String url, Object body, Class<T> type) {
        try {
            T result = webClient.post()
                    .uri(url)
                    .header("X-Internal-Token", properties.getInternalToken())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(ApiResponseWrapper.class)
                    .map(wrapper -> wrapper.to(type))
                    .timeout(Duration.ofSeconds(2))
                    .onErrorResume(ex -> reactor.core.publisher.Mono.empty())
                    .block();
            return Optional.ofNullable(result);
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private void postBestEffort(String url, Object body) {
        try {
            webClient.post()
                    .uri(url)
                    .header("X-Internal-Token", properties.getInternalToken())
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(2))
                    .onErrorResume(ex -> reactor.core.publisher.Mono.empty())
                    .block();
        } catch (RuntimeException ignored) {
            // Collaborator services may be offline during local development.
        }
    }

    private record ApiResponseWrapper(int code, Object data) {
        <T> T to(Class<T> type) {
            if (code != 200 || data == null) {
                return null;
            }
            return JsonMapperHolder.convert(data, type);
        }
    }
}
