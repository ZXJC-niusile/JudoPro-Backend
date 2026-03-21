package cn.edu.bistu.cs.ir.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 火山引擎多模态API配置类
 */
@Component
@ConfigurationProperties(prefix = "volcengine")
public class VolcengineConfig {

    /**
     * 火山引擎API基础URL
     */
    private String baseUrl = "https://api.volcengine.com";

    /**
     * 多模态API端点
     */
    private String multimodalApiEndpoint = "/v1/multimodal/analyze";

    /**
     * 聊天引擎API密钥
     */
    private String apiKey = "80e00021-76e9-4844-b98b-2d342a17e164";

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 30000;

    /**
     * 写入超时时间（毫秒）
     */
    private int writeTimeout = 300000; // 5分钟，支持大文件上传

    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 300000; // 5分钟，支持大文件处理

    /**
     * 最大重试次数
     */
    private int maxRetryAttempts = 3;

    /**
     * 重试间隔时间（毫秒）
     */
    private int retryInterval = 5000;

    /**
     * 是否启用二进制流传输
     */
    private boolean useBinaryTransmission = true;

    /**
     * 是否使用多模态API
     */
    private boolean useMultimodalApi = true;

    /**
     * 最大文件大小限制（字节）
     */
    private long maxFileSize = 10L * 1024 * 1024 * 1024; // 10GB

    /**
     * 分片上传的块大小（字节）
     */
    private int chunkSize = 5 * 1024 * 1024; // 5MB

    /**
     * 获取完整的多模态API URL
     */
    public String getMultimodalApiUrl() {
        return baseUrl + multimodalApiEndpoint;
    }

    /**
     * 获取传统Chat Completions API URL（向后兼容）
     */
    public String getChatCompletionsUrl() {
        return baseUrl + "/v3/chat/completions";
    }

    // Getters and Setters
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getMultimodalApiEndpoint() {
        return multimodalApiEndpoint;
    }

    public void setMultimodalApiEndpoint(String multimodalApiEndpoint) {
        this.multimodalApiEndpoint = multimodalApiEndpoint;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public boolean isUseBinaryTransmission() {
        return useBinaryTransmission;
    }

    public void setUseBinaryTransmission(boolean useBinaryTransmission) {
        this.useBinaryTransmission = useBinaryTransmission;
    }

    public boolean isUseMultimodalApi() {
        return useMultimodalApi;
    }

    public void setUseMultimodalApi(boolean useMultimodalApi) {
        this.useMultimodalApi = useMultimodalApi;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }
}