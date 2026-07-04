package com.bangboo.ai.client;

import com.bangboo.ai.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 通义万相（阿里云 DashScope）文生图 / 图生图客户端。
 * 模型：wan2.7-image-pro，采用 DashScope 异步任务协议：
 *   1) POST /api/v1/services/aigc/image-generation/generation 提交任务 -> 得到 task_id
 *      采用 messages 格式，X-DashScope-Async: enable
 *   2) 轮询 GET /api/v1/tasks/{task_id} 直到 task_status = SUCCEEDED，取 choices[0].message.content[0].image
 * 任何异常/超时向上抛出，由上层降级为占位图。
 */
@Component
public class WanxImageClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(WanxImageClient.class);
    private static final String SUBMIT_PATH = "/api/v1/services/aigc/image-generation/generation";
    private static final String TASK_PATH = "/api/v1/tasks/";

    private final WebClient webClient;
    private final AiProperties properties;

    public WanxImageClient(WebClient webClient, AiProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    public boolean enabled() {
        return properties.isWanxEnabled();
    }

    /**
     * 文生图 / 图生图：提交任务 → 轮询 → 下载到本地 → 返回本地可访问路径。
     * @param prompt              正向提示词
     * @param referenceImageBytes 参考图片二进制（图生图），为 null 则为纯文生图
     * @param subDir              本地存储子目录（如 "cover"、"avatar"）
     * @return 本地访问路径（如 /uploads/ai/cover/abc123.png）
     */
    public String generate(String prompt, byte[] referenceImageBytes, String subDir) {
        String cloudUrl = generateRaw(prompt, referenceImageBytes);
        return saveToLocal(cloudUrl, subDir);
    }

    /**
     * 仅提交任务并轮询，返回 DashScope 云端临时 URL（不下载到本地）。
     */
    String generateRaw(String prompt, byte[] referenceImageBytes) {
        AiProperties.Wanx w = properties.getWanx();
        Duration timeout = Duration.ofSeconds(Math.max(5, w.getTimeoutSeconds()));

        // 构建 content 数组
        List<Map<String, Object>> contentItems = new ArrayList<>();

        // 图生图：参考图片放在最前面，每次请求仅一张
        if (referenceImageBytes != null && referenceImageBytes.length > 0) {
            String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(referenceImageBytes);
            contentItems.add(Map.of("image", (Object) base64));
        }

        // 提示词文本
        contentItems.add(Map.of("text", (Object) (prompt == null ? "" : prompt)));

        Map<String, Object> input = Map.of("messages", List.of(
                Map.of("role", "user", "content", (Object) contentItems)
        ));

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("size", w.getSize());
        params.put("n", 1);
        params.put("watermark", false);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", w.getModel());
        body.put("input", input);
        body.put("parameters", params);

        String submitRaw = webClient.post()
                .uri(w.getBaseUrl() + SUBMIT_PATH)
                .header("Authorization", "Bearer " + w.getApiKey())
                .header("X-DashScope-Async", "enable")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(timeout)
                .block();

        String taskId = readTaskId(submitRaw);
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalStateException("通义万相未返回 task_id: " + submitRaw);
        }

        // 轮询任务结果
        for (int i = 0; i < w.getMaxPollTimes(); i++) {
            sleep(w.getPollIntervalMs());
            String taskRaw = webClient.get()
                    .uri(w.getBaseUrl() + TASK_PATH + taskId)
                    .header("Authorization", "Bearer " + w.getApiKey())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(timeout)
                    .block();

            JsonNode output = readTree(taskRaw).path("output");
            String status = output.path("task_status").asText("");
            if ("SUCCEEDED".equalsIgnoreCase(status)) {
                String url = firstImageUrl(output);
                if (url == null || url.isBlank()) {
                    throw new IllegalStateException("通义万相任务成功但无图片 URL: " + taskRaw);
                }
                return url;
            }
            if ("FAILED".equalsIgnoreCase(status) || "UNKNOWN".equalsIgnoreCase(status)) {
                throw new IllegalStateException("通义万相任务失败: " + output.path("message").asText(status));
            }
        }
        throw new IllegalStateException("通义万相任务轮询超时，taskId=" + taskId);
    }

    /**
     * 下载云端图片到本地。
     */
    private String saveToLocal(String cloudUrl, String subDir) {
        Path uploadRoot = Path.of(properties.getUploadDir());
        Path targetDir = uploadRoot.resolve(subDir);
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new IllegalStateException("创建存储目录失败: " + targetDir, e);
        }

        String ext = extractExtension(cloudUrl);
        String filename = UUID.randomUUID().toString().replace("-", "").substring(0, 12) + ext;
        Path targetFile = targetDir.resolve(filename);

        try {
            byte[] imageBytes = webClient.get()
                    .uri(URI.create(cloudUrl))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            if (imageBytes == null || imageBytes.length == 0) {
                throw new IllegalStateException("下载图片失败：响应体为空");
            }
            Files.write(targetFile, imageBytes);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("下载/保存图片失败 cloudUrl={} targetFile={}", cloudUrl, targetFile, e);
            throw new IllegalStateException("下载或保存图片失败: " + e.getMessage(), e);
        }

        String localPath = properties.getAssetBaseUrl() + "/" + subDir + "/" + filename;
        log.info("图片已保存到本地: {}", localPath);
        return localPath;
    }

    /** 下载参考图并转为字节数组。url 可以是相对路径（如 /uploads/...），自动拼接 baseUrl。 */
    public byte[] fetchReferenceImageBytes(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return null;
        String fullUrl = imageUrl.startsWith("http") ? imageUrl
                : properties.getFileBaseUrl() + imageUrl;
        try {
            byte[] bytes = webClient.get()
                    .uri(URI.create(fullUrl))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .timeout(Duration.ofSeconds(15))
                    .block();
            if (bytes == null || bytes.length == 0) {
                log.warn("参考图下载结果为空: {}", fullUrl);
                return null;
            }
            log.info("参考图已下载: {} ({} bytes)", fullUrl, bytes.length);
            return bytes;
        } catch (Exception e) {
            log.error("下载参考图失败 url={}: {}", fullUrl, e.getMessage());
            return null;
        }
    }

    private String extractExtension(String url) {
        if (url == null || url.isBlank()) return ".png";
        int q = url.indexOf('?');
        String path = q > 0 ? url.substring(0, q) : url;
        int dot = path.lastIndexOf('.');
        if (dot < 0) return ".png";
        String ext = path.substring(dot).toLowerCase();
        if (ext.matches("\\.(png|jpg|jpeg|webp|gif)")) return ext;
        return ".png";
    }

    private String readTaskId(String raw) {
        return readTree(raw).path("output").path("task_id").asText(null);
    }

    private String firstImageUrl(JsonNode output) {
        JsonNode choices = output.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            JsonNode content = choices.get(0).path("message").path("content");
            if (content.isArray() && content.size() > 0) {
                String url = content.get(0).path("image").asText("");
                if (!url.isBlank()) return url;
            }
        }
        return null;
    }

    private JsonNode readTree(String raw) {
        try {
            return MAPPER.readTree(raw == null ? "{}" : raw);
        } catch (Exception ex) {
            throw new IllegalStateException("解析通义万相响应失败: " + ex.getMessage(), ex);
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("轮询被中断", e);
        }
    }
}
