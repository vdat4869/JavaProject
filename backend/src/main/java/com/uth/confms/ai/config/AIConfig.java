package com.uth.confms.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * Configuration class cho AI Module.
 * Load API keys và cấu hình HTTP client cho OpenAI.
 */
@Configuration
public class AIConfig {

    @Value("${ai.openai.api-key:}")
    private String openaiApiKey;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String openaiModel;

    @Value("${ai.openai.base-url:https://api.openai.com/v1}")
    private String openaiBaseUrl;

    @Value("${ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${ai.timeout-seconds:30}")
    private int timeoutSeconds;

    @Bean(name = "aiRestTemplate")
    public RestTemplate aiRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutSeconds * 1000);
        factory.setReadTimeout(timeoutSeconds * 1000);
        return new RestTemplate(factory);
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

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public boolean isConfigured() {
        return aiEnabled && openaiApiKey != null && !openaiApiKey.isBlank();
    }
}
