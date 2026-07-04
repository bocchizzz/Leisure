package com.bangboo.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bb.ai")
public class AiProperties {
    private String internalToken = "bangboo-internal-dev-token";
    private String courtBaseUrl = "http://127.0.0.1:8086";
    private String adminOpsBaseUrl = "http://127.0.0.1:8088";
    /** 文件服务的访问基地址，用于将前端传来的相对图片路径拼成全 URL 后下载。 */
    private String fileBaseUrl = "http://127.0.0.1:8080";
    private String assetBaseUrl = "/uploads/ai";
    /** AI 生成图片的本地存储目录（绝对路径或相对于工作目录）。 */
    private String uploadDir = "data/ai-images";

    /** 生成方式：rule=规则/模板（默认，零依赖）；llm=接入真实大模型（DeepSeek）。 */
    private String provider = "rule";

    /** 真实大模型（DeepSeek，OpenAI 兼容协议）配置。 */
    private Llm llm = new Llm();

    /** 文生图（通义万相，阿里云 DashScope）配置。 */
    private Wanx wanx = new Wanx();

    public String getInternalToken() {
        return internalToken;
    }

    public void setInternalToken(String internalToken) {
        this.internalToken = internalToken;
    }

    public String getCourtBaseUrl() {
        return courtBaseUrl;
    }

    public void setCourtBaseUrl(String courtBaseUrl) {
        this.courtBaseUrl = courtBaseUrl;
    }

    public String getAdminOpsBaseUrl() {
        return adminOpsBaseUrl;
    }

    public void setAdminOpsBaseUrl(String adminOpsBaseUrl) {
        this.adminOpsBaseUrl = adminOpsBaseUrl;
    }

    public String getFileBaseUrl() {
        return fileBaseUrl;
    }

    public void setFileBaseUrl(String fileBaseUrl) {
        this.fileBaseUrl = fileBaseUrl;
    }

    public String getAssetBaseUrl() {
        return assetBaseUrl;
    }

    public void setAssetBaseUrl(String assetBaseUrl) {
        this.assetBaseUrl = assetBaseUrl;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Llm getLlm() {
        return llm;
    }

    public void setLlm(Llm llm) {
        this.llm = llm;
    }

    public Wanx getWanx() {
        return wanx;
    }

    public void setWanx(Wanx wanx) {
        this.wanx = wanx;
    }

    /** 判断是否启用真实大模型（provider=llm 且 apiKey 非空）。 */
    public boolean isLlmEnabled() {
        return "llm".equalsIgnoreCase(provider)
                && llm != null
                && llm.getApiKey() != null
                && !llm.getApiKey().isBlank();
    }

    /** 判断是否启用通义万相文生图（配置了非空 apiKey）。 */
    public boolean isWanxEnabled() {
        return wanx != null
                && wanx.getApiKey() != null
                && !wanx.getApiKey().isBlank();
    }

    public static class Llm {
        /** DeepSeek base-url（OpenAI 兼容），在 application.yml 中配置。 */
        private String baseUrl = "https://api.deepseek.com";
        /** DeepSeek API Key，在 application.yml 的 bb.ai.llm.api-key 中配置。 */
        private String apiKey = "";
        /** 模型名。 */
        private String model = "deepseek-chat";
        /** 提示词版本，用于落表追溯。 */
        private String promptVersion = "v1";
        /** 调用超时秒数。 */
        private int timeoutSeconds = 30;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getPromptVersion() {
            return promptVersion;
        }

        public void setPromptVersion(String promptVersion) {
            this.promptVersion = promptVersion;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }
    }

    /** 通义万相文生图配置（阿里云 DashScope 异步任务协议）。 */
    public static class Wanx {
        /** DashScope 服务根地址。 */
        private String baseUrl = "https://dashscope.aliyuncs.com";
        /** DashScope API Key，在 application.yml 的 bb.ai.wanx.api-key 中配置（与 DeepSeek 的不同）。 */
        private String apiKey = "";
        /** 文生图模型名。 */
        private String model = "wanx2.1-t2i-turbo";
        /** 生成图片尺寸（宽*高）。 */
        private String size = "1024*1024";
        /** 提交任务后轮询结果的最大次数。 */
        private int maxPollTimes = 15;
        /** 每次轮询间隔毫秒。 */
        private long pollIntervalMs = 2000;
        /** 单次 HTTP 请求超时秒数。 */
        private int timeoutSeconds = 30;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public int getMaxPollTimes() {
            return maxPollTimes;
        }

        public void setMaxPollTimes(int maxPollTimes) {
            this.maxPollTimes = maxPollTimes;
        }

        public long getPollIntervalMs() {
            return pollIntervalMs;
        }

        public void setPollIntervalMs(long pollIntervalMs) {
            this.pollIntervalMs = pollIntervalMs;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }
    }
}
