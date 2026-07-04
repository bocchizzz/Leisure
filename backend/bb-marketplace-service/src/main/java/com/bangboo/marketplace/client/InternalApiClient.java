package com.bangboo.marketplace.client;

import com.bangboo.marketplace.config.MarketplaceProperties;
import com.bangboo.marketplace.dto.internal.CreateAuditLogRequest;
import com.bangboo.marketplace.dto.internal.CreateMessageRequest;
import com.bangboo.marketplace.dto.internal.OpsConfigVO;
import com.bangboo.marketplace.dto.internal.UserAccessStateVO;
import com.bangboo.marketplace.dto.internal.UserBriefVO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * 服务间内部调用客户端。
 * 读接口：超时 2s，失败返回 Optional.empty（不阻塞开发）。
 * 写接口：best-effort，失败不抛出（消息/审计失败不回滚主业务）。
 */
@Component
public class InternalApiClient {
    private final WebClient webClient;
    private final MarketplaceProperties properties;

    public InternalApiClient(WebClient webClient, MarketplaceProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    /** 用户简要信息，用于保存快照。 */
    public Optional<UserBriefVO> userBrief(Long userId) {
        return getOptional(properties.getIamBaseUrl() + "/internal/users/" + userId + "/brief", UserBriefVO.class);
    }

    /** 用户访问状态，用于发布/申请前二次校验。 */
    public Optional<UserAccessStateVO> userAccessState(Long userId) {
        return getOptional(properties.getIamBaseUrl() + "/internal/users/" + userId + "/access-state", UserAccessStateVO.class);
    }

    /** 运营配置，用于内容安全阈值/关键词；不可用时返回默认。 */
    public OpsConfigVO opsConfig() {
        return getOptional(properties.getAdminOpsBaseUrl() + "/internal/ops-config", OpsConfigVO.class)
                .orElseGet(OpsConfigVO::defaults);
    }

    public void createMessage(CreateMessageRequest request) {
        postBestEffort(properties.getMessageBaseUrl() + "/internal/messages", request);
    }

    public void createAuditLog(CreateAuditLogRequest request) {
        postBestEffort(properties.getAdminOpsBaseUrl() + "/internal/audit-logs", request);
    }

    /**
     * 调用 reputation 的信誉变更接口（记流水 + 调 IAM 更新信誉分）。
     * best-effort：第一版失败仅记日志，不回滚主业务。
     */
    public void adjustCredit(Long userId, int delta, String sourceType, Long sourceId, String reason) {
        Map<String, Object> body = Map.of(
                "userId", userId,
                "delta", delta,
                "sourceType", sourceType,
                "sourceId", sourceId == null ? 0 : sourceId,
                "reason", reason == null ? "" : reason
        );
        postBestEffort(properties.getReputationBaseUrl() + "/internal/credit-adjustments", body);
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
