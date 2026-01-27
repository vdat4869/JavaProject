package com.uth.confms.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
 * AI Gateway Service - Giao tiếp với OpenAI hoặc Google Gemini.
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

    public AIResponse chat(String systemPrompt, String userPrompt) {
        if (!aiConfig.isConfigured()) {
            throw new IllegalStateException("AI is not configured correctly.");
        }

        if (aiConfig.isMockMode()) {
            log.info("AI Mock Mode: Generating simulated response.");
            String mockContent = generateMockResponse(systemPrompt, userPrompt);
            return AIResponse.success(mockContent, 100, 0);
        }

        if ("gemini".equalsIgnoreCase(aiConfig.getProvider())) {
            return callGemini(systemPrompt, userPrompt);
        } else {
            return callOpenAI(systemPrompt, userPrompt);
        }
    }

    private AIResponse callOpenAI(String systemPrompt, String userPrompt) {
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
                    "temperature", 0.7);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    aiConfig.getOpenaiBaseUrl() + "/chat/completions",
                    HttpMethod.POST,
                    entity,
                    String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String content = jsonNode.path("choices").get(0).path("message").path("content").asText();
            int tokens = jsonNode.path("usage").path("total_tokens").asInt(0);

            return AIResponse.success(content, System.currentTimeMillis() - startTime, tokens);
        } catch (Exception e) {
            log.error("OpenAI error: {}", e.getMessage());
            return AIResponse.error(e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    private AIResponse callGemini(String systemPrompt, String userPrompt) {
        long startTime = System.currentTimeMillis();
        try {
            // Gemini expects system instructions and user prompt in a specific format
            // Combine them for simplicity or use specific fields if using beta features
            String combinedPrompt = systemPrompt + "\n\nUser Input:\n" + userPrompt;

            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode contents = requestBody.putArray("contents");
            ObjectNode contentObj = contents.addObject();
            ArrayNode parts = contentObj.putArray("parts");
            parts.addObject().put("text", combinedPrompt);

            // Add safety settings or generation config if needed
            ObjectNode generationConfig = requestBody.putObject("generationConfig");
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 2048);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            String url = String.format("%s/models/%s:generateContent?key=%s",
                    aiConfig.getGeminiBaseUrl().trim(),
                    aiConfig.getGeminiModel().trim(),
                    aiConfig.getGeminiApiKey().trim());

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            return AIResponse.success(content, System.currentTimeMillis() - startTime, 0);
        } catch (Exception e) {
            log.error("Gemini error: {}", e.getMessage());
            return AIResponse.error(e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    private String generateMockResponse(String systemPrompt, String userPrompt) {
        if (systemPrompt.contains("summary")) {
            return "{\"summary\": \"Đây là bản tóm tắt mô phỏng (Gemini Mock Mode).\", \"tone\": \"Trang trọng\"}";
        }
        return "{\"claims\": [\"Tính năng mock mới\"], \"methods\": [], \"datasets\": [], \"findings\": []}";
    }

    public boolean isAvailable() {
        return aiConfig.isConfigured();
    }

    public static class AIResponse {
        private final boolean success;
        private final String content;
        private final String errorMessage;
        private final long processingTimeMs;
        private final int tokensUsed;

        private AIResponse(boolean success, String content, String errorMessage, long processingTimeMs,
                int tokensUsed) {
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
