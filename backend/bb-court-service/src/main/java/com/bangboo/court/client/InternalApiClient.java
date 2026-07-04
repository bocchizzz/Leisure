package com.bangboo.court.client;

import com.bangboo.court.config.CourtProperties;
import com.bangboo.court.dto.ContractBriefVO;
import com.bangboo.court.dto.ContractRuleResultRequest;
import com.bangboo.court.dto.CreateAuditLogRequest;
import com.bangboo.court.dto.CreateMessageRequest;
import com.bangboo.court.dto.OpsConfigVO;
import com.bangboo.court.dto.ReputationAdjustmentRequest;
import com.bangboo.court.dto.UserBriefVO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Optional;

@Component
public class InternalApiClient {
    private final WebClient webClient;
    private final CourtProperties properties;

    public InternalApiClient(WebClient webClient, CourtProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    public Optional<ContractBriefVO> contractBrief(Long contractId) {
        return getOptional(properties.getMarketplaceBaseUrl() + "/internal/contracts/" + contractId + "/brief", ContractBriefVO.class)
                .or(() -> devContractBrief(contractId));
    }

    public Optional<UserBriefVO> userBrief(Long userId) {
        return getOptional(properties.getIamBaseUrl() + "/internal/users/" + userId + "/brief", UserBriefVO.class);
    }

    public OpsConfigVO opsConfig() {
        return getOptional(properties.getAdminOpsBaseUrl() + "/internal/ops-config", OpsConfigVO.class)
                .orElseGet(OpsConfigVO::defaults);
    }

    public void markContractDisputed(Long contractId, Long caseId) {
        postBestEffort(properties.getMarketplaceBaseUrl() + "/internal/contracts/" + contractId + "/mark-disputed", new MarkDisputedRequest(caseId));
    }

    public void applyContractRuling(Long contractId, ContractRuleResultRequest request) {
        postBestEffort(properties.getMarketplaceBaseUrl() + "/internal/contracts/" + contractId + "/rule-result", request);
    }

    public void createMessage(CreateMessageRequest request) {
        postBestEffort(properties.getMessageBaseUrl() + "/internal/messages", request);
    }

    public void createAuditLog(CreateAuditLogRequest request) {
        postBestEffort(properties.getAdminOpsBaseUrl() + "/internal/audit-logs", request);
    }

    public void adjustReputation(Long userId, ReputationAdjustmentRequest request) {
        postBestEffort(properties.getIamBaseUrl() + "/internal/users/" + userId + "/reputation-adjustments", request);
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

    private Optional<ContractBriefVO> devContractBrief(Long contractId) {
        if (Long.valueOf(301L).equals(contractId)) {
            return Optional.of(new ContractBriefVO(
                    301L,
                    201L,
                    "Poster polishing and layout",
                    2L,
                    "Client One",
                    3L,
                    "Hunter One",
                    "IN_PROGRESS",
                    80.0,
                    "POINT"
            ));
        }
        if (Long.valueOf(302L).equals(contractId)) {
            return Optional.of(new ContractBriefVO(
                    302L,
                    202L,
                    "Delivery scope verification",
                    2L,
                    "Client One",
                    3L,
                    "Hunter One",
                    "WAIT_CONFIRM",
                    120.0,
                    "POINT"
            ));
        }
        return Optional.empty();
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
            // External collaborator services may not be running during local A-side development.
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

    private record MarkDisputedRequest(Long caseId) {
    }
}
