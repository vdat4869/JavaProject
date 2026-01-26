package com.uth.confms.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uth.confms.ai.config.AIConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * AI Gateway Service - Giao tiếp với OpenAI API.
 * Đây là service trung tâm xử lý tất cả các request đến AI.
 */
@Service
public class AIGatewayService {

    private static final Logger log = LoggerFactory.getLogger(AIGatewayService.class);

    private final AIConfig aiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AIGatewayService(
            AIConfig aiConfig,
            @Qualifier("aiRestTemplate") RestTemplate restTemplate) {
        this.aiConfig = aiConfig;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Gọi OpenAI Chat Completion API.
     *
     * @param systemPrompt System prompt để định hướng AI
     * @param userPrompt   User prompt (nội dung cần xử lý)
     * @return Response từ AI
     */
    public AIResponse chat(String systemPrompt, String userPrompt) {
        if (!aiConfig.isConfigured()) {
            throw new IllegalStateException("AI is not configured. Please set AI_OPENAI_API_KEY.");
        }

        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiConfig.getOpenaiApiKey());

            Map<String, Object> requestBody = Map.of(
                    "model", aiConfig.getOpenaiModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)),
                    "temperature", 0.7,
                    "max_tokens", 2000);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    aiConfig.getOpenaiBaseUrl() + "/chat/completions",
                    HttpMethod.POST,
                    entity,
                    String.class);

            long processingTime = System.currentTimeMillis() - startTime;

            // Parse response
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String content = jsonNode
                    .path("choices").get(0)
                    .path("message")
                    .path("content")
                    .asText();

            int totalTokens = jsonNode.path("usage").path("total_tokens").asInt(0);

            return AIResponse.success(content, processingTime, totalTokens);

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("AI Gateway error: {}", e.getMessage(), e);
            return AIResponse.error(e.getMessage(), processingTime);
        }
    }

    /**
     * Kiểm tra AI service có sẵn sàng không.
     */
    public boolean isAvailable() {
        return aiConfig.isConfigured();
    }

    /**
     * Response wrapper cho AI calls.
     */
    public static class AIResponse {
        private final boolean success;
        private final String content;
        private final String errorMessage;
        private final long processingTimeMs;
        private final int tokensUsed;

        private AIResponse(boolean success, String content, String errorMessage,
                long processingTimeMs, int tokensUsed) {
            this.success = success;
            this.content = content;
            this.errorMessage = errorMessage;
            this.processingTimeMs = processingTimeMs;
            this.tokensUsed = tokensUsed;
        }

        public static AIResponse success(String content, long processingTimeMs, int tokensUsed) {
            return new AIResponse(true, content, null, processingTimeMs, tokensUsed);
        }

        public static AIResponse error(String errorMessage, long processingTimeMs) {
            return new AIResponse(false, null, errorMessage, processingTimeMs, 0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getContent() {
            return content;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public long getProcessingTimeMs() {
            return processingTimeMs;
        }

        public int getTokensUsed() {
            return tokensUsed;
        }
    }
}
