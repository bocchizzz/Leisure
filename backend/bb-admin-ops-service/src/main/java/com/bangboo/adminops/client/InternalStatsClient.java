package com.bangboo.adminops.client;

import com.bangboo.adminops.config.AdminOpsProperties;
import com.bangboo.adminops.dto.AiStatsVO;
import com.bangboo.adminops.dto.IamStatsVO;
import com.bangboo.adminops.dto.MarketplaceStatsVO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
public class InternalStatsClient {
    private final WebClient webClient;
    private final AdminOpsProperties properties;

    public InternalStatsClient(WebClient webClient, AdminOpsProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    public IamStatsVO iamStats() {
        return getOrDefault(properties.getIamBaseUrl() + "/internal/users/stats", IamStatsVO.class, new IamStatsVO(0, 0));
    }

    public MarketplaceStatsVO marketplaceStats() {
        return getOrDefault(
                properties.getMarketplaceBaseUrl() + "/internal/stats/marketplace",
                MarketplaceStatsVO.class,
                new MarketplaceStatsVO(0, 0, 0, 0, 0)
        );
    }

    public AiStatsVO aiStats() {
        return getOrDefault(properties.getAiBaseUrl() + "/internal/stats/ai", AiStatsVO.class, new AiStatsVO(0));
    }

    private <T> T getOrDefault(String url, Class<T> type, T fallback) {
        try {
            return webClient.get()
                    .uri(url)
                    .header("X-Internal-Token", properties.getInternalToken())
                    .retrieve()
                    .bodyToMono(ApiResponseWrapper.class)
                    .map(wrapper -> wrapper.to(type, fallback))
                    .timeout(Duration.ofSeconds(2))
                    .onErrorReturn(fallback)
                    .block();
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private record ApiResponseWrapper(int code, Object data) {
        <T> T to(Class<T> type, T fallback) {
            if (code != 200 || data == null) {
                return fallback;
            }
            return JsonMapperHolder.convert(data, type);
        }
    }
}
