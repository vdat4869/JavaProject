package com.uth.confms.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * Configuration class cho AI Module.
 * Hỗ trợ cả OpenAI và Google Gemini.
 */
@Configuration
public class AIConfig {

    @Value("${ai.provider:gemini}")
    private String provider; // Nhà cung cấp dịch vụ AI (gemini hoặc openai)

    @Value("${ai.openai.api-key:}")
    private String openaiApiKey; // API key cho OpenAI

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String openaiModel; // Model mặc định của OpenAI

    @Value("${ai.openai.base-url:https://api.openai.com/v1}")
    private String openaiBaseUrl; // Endpoint của OpenAI

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey; // API key cho Google Gemini

    @Value("${ai.gemini.model:gemini-2.5-flash}")
    private String geminiModel; // Model mặc định của Gemini

    @Value("${ai.gemini.base-url:https://generativelanguage.googleapis.com/v1}")
    private String geminiBaseUrl; // Endpoint của Gemini

    @Value("${ai.enabled:false}")
    private boolean aiEnabled; // Trạng thái kích hoạt module AI

    @Value("${ai.timeout-seconds:30}")
    private int timeoutSeconds; // Thời gian timeout cho các lời gọi API (giây)

    @Value("${ai.mock-mode:false}")
    private boolean mockMode; // Chế độ giả lập (không gọi API thật)

    /**
     * Khởi tạo RestTemplate dùng riêng cho các lời gọi AI với cấu hình timeout.
     */
    @Bean(name = "aiRestTemplate")
    public RestTemplate aiRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutSeconds * 1000);
        factory.setReadTimeout(timeoutSeconds * 1000);
        return new RestTemplate(factory);
    }

    public String getProvider() {
        return provider;
    }

    public String getOpenaiApiKey() {
        return openaiApiKey;
    }

    public String getOpenaiModel() {
        return openaiModel;
    }

    public String getOpenaiBaseUrl() {
        return openaiBaseUrl;
    }

    public String getGeminiApiKey() {
        return geminiApiKey;
    }

    public String getGeminiModel() {
        return geminiModel;
    }

    public String getGeminiBaseUrl() {
        return geminiBaseUrl;
    }

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public boolean isMockMode() {
        return mockMode;
    }

    public boolean isConfigured() {
        if (mockMode)
            return true;
        if (!aiEnabled)
            return false;

        if ("gemini".equalsIgnoreCase(provider)) {
            return geminiApiKey != null && !geminiApiKey.isBlank();
        } else {
            return openaiApiKey != null && !openaiApiKey.isBlank();
        }
    }

    public String getCurrentModel() {
        if (mockMode)
            return "mock-model";
        return "gemini".equalsIgnoreCase(provider) ? geminiModel : openaiModel;
    }
}
