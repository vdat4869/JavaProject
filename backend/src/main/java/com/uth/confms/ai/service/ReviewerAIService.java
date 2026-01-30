package com.uth.confms.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uth.confms.ai.dto.*;
import com.uth.confms.ai.entity.AIAuditLog;
import com.uth.confms.ai.enums.AIFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý các tính năng AI cho Reviewer/PC.
 * Bao gồm: Neutral Summary, Key Points, Similarity Hint.
 */
@Service
public class ReviewerAIService {

        private static final Logger log = LoggerFactory.getLogger(ReviewerAIService.class);

        private final AIGatewayService aiGateway;
        private final AIAuditService auditService;
        private final ObjectMapper objectMapper;

        public ReviewerAIService(AIGatewayService aiGateway, AIAuditService auditService) {
                this.aiGateway = aiGateway;
                this.auditService = auditService;
                this.objectMapper = new ObjectMapper();
        }

        /**
         * Tạo bản tóm tắt bài báo với ngôn ngữ trung lập (không đánh giá hay/dở).
         * Hỗ trợ PC Member nhanh chóng nắm bắt nội dung để thực hiện bidding.
         */
        public NeutralSummaryResponse generateNeutralSummary(NeutralSummaryRequest request, Long userId) {
                String systemPrompt = """
                                You are an academic summarizer. Create a neutral, objective summary of the following abstract.

                                IMPORTANT RULES:
                                1. DO NOT use evaluative language (e.g., "good", "novel", "significant", "important")
                                2. DO NOT make judgments about quality or contribution
                                3. Only describe WHAT the paper does, not HOW WELL it does it
                                4. Use passive voice and objective tone
                                5. Keep it between 150-250 words

                                Return only the summary text, no JSON or formatting.
                                """;

                String userPrompt = "Abstract to summarize:\n\n" + request.getAbstractText();

                AIGatewayService.AIResponse aiResponse = aiGateway.chat(systemPrompt, userPrompt);

                if (!aiResponse.isSuccess()) {
                        auditService.createErrorLog(userId, request.getConferenceId(), AIFeature.NEUTRAL_SUMMARY,
                                        aiGateway.getCurrentModelName(), userPrompt, aiResponse.getErrorMessage(),
                                        aiResponse.getProcessingTimeMs());

                        return NeutralSummaryResponse.builder()
                                        .success(false)
                                        .message("AI service error: " + aiResponse.getErrorMessage())
                                        .build();
                }

                String summary = aiResponse.getContent().trim();
                int wordCount = summary.split("\\s+").length;

                AIAuditLog auditLog = auditService.createAuditLog(
                                userId, request.getConferenceId(), AIFeature.NEUTRAL_SUMMARY,
                                aiGateway.getCurrentModelName(), userPrompt, summary,
                                aiResponse.getProcessingTimeMs(), aiResponse.getTokensUsed());

                return NeutralSummaryResponse.builder()
                                .success(true)
                                .message("Neutral summary generated")
                                .summary(summary)
                                .wordCount(wordCount)
                                .auditLogId(auditLog.getId())
                                .processingTimeMs(aiResponse.getProcessingTimeMs())
                                .build();
        }

        /**
         * Trích xuất các key points từ abstract.
         */
        public KeyPointsResponse extractKeyPoints(KeyPointsRequest request, Long userId) {
                String systemPrompt = """
                                You are an academic paper analyzer. Extract key points from the following abstract.

                                Return a JSON object with:
                                - claims: array of main claims/contributions (1-3 items)
                                - methods: array of methods/approaches mentioned (1-3 items)
                                - datasets: array of datasets used if mentioned (can be empty)
                                - findings: array of key results/findings (1-3 items)

                                Keep each item concise (1-2 sentences max).
                                Only return valid JSON, no other text.
                                """;

                String userPrompt = "Abstract:\n\n" + request.getAbstractText();

                AIGatewayService.AIResponse aiResponse = aiGateway.chat(systemPrompt, userPrompt);

                if (!aiResponse.isSuccess()) {
                        auditService.createErrorLog(userId, request.getConferenceId(), AIFeature.KEY_POINTS,
                                        aiGateway.getCurrentModelName(), userPrompt, aiResponse.getErrorMessage(),
                                        aiResponse.getProcessingTimeMs());

                        return KeyPointsResponse.builder()
                                        .success(false)
                                        .message("AI service error: " + aiResponse.getErrorMessage())
                                        .build();
                }

                // Parse AI response
                List<String> claims = new ArrayList<>();
                List<String> methods = new ArrayList<>();
                List<String> datasets = new ArrayList<>();
                List<String> findings = new ArrayList<>();

                try {
                        String content = aiResponse.getContent().trim();
                        if (content.startsWith("```")) {
                                content = content.replaceAll("```json\\n?", "").replaceAll("```\\n?", "");
                        }
                        Map<String, Object> parsed = objectMapper.readValue(content,
                                        new TypeReference<Map<String, Object>>() {
                                        });

                        claims = getStringList(parsed, "claims");
                        methods = getStringList(parsed, "methods");
                        datasets = getStringList(parsed, "datasets");
                        findings = getStringList(parsed, "findings");
                } catch (Exception e) {
                        log.warn("Failed to parse AI response: {}", e.getMessage());
                }

                AIAuditLog auditLog = auditService.createAuditLog(
                                userId, request.getConferenceId(), AIFeature.KEY_POINTS,
                                aiGateway.getCurrentModelName(), userPrompt, aiResponse.getContent(),
                                aiResponse.getProcessingTimeMs(), aiResponse.getTokensUsed());

                return KeyPointsResponse.builder()
                                .success(true)
                                .message("Key points extracted")
                                .claims(claims)
                                .methods(methods)
                                .datasets(datasets)
                                .findings(findings)
                                .auditLogId(auditLog.getId())
                                .processingTimeMs(aiResponse.getProcessingTimeMs())
                                .build();
        }

        /**
         * Tính toán gợi ý độ tương đồng giữa chuyên môn của Reviewer và nội dung bài
         * báo.
         * Hỗ trợ Chair trong việc phân công reviewer phù hợp nhất.
         */
        public SimilarityHintResponse calculateSimilarityHint(SimilarityHintRequest request, Long userId) {
                String systemPrompt = """
                                You are an academic paper-reviewer matching assistant.
                                Compare the paper's topics/keywords with the reviewer's expertise.

                                Return a JSON object with:
                                - similarityScore: 0.0 to 1.0
                                - overlappingKeywords: array of matching keywords/topics
                                - fitLevel: "HIGH", "MEDIUM", or "LOW"
                                - explanation: brief explanation of the match

                                Consider:
                                - Exact keyword matches
                                - Related/synonymous terms
                                - Broader field matches

                                Only return valid JSON, no other text.
                                """;

                String userPrompt = String.format("""
                                Paper keywords: %s
                                Paper topics: %s

                                Reviewer expertise: %s
                                """,
                                request.getPaperKeywords() != null ? String.join(", ", request.getPaperKeywords())
                                                : "N/A",
                                request.getPaperTopics() != null ? String.join(", ", request.getPaperTopics()) : "N/A",
                                request.getReviewerExpertise() != null
                                                ? String.join(", ", request.getReviewerExpertise())
                                                : "N/A");

                AIGatewayService.AIResponse aiResponse = aiGateway.chat(systemPrompt, userPrompt);

                if (!aiResponse.isSuccess()) {
                        auditService.createErrorLog(userId, request.getConferenceId(), AIFeature.SIMILARITY_HINT,
                                        aiGateway.getCurrentModelName(), userPrompt, aiResponse.getErrorMessage(),
                                        aiResponse.getProcessingTimeMs());

                        return SimilarityHintResponse.builder()
                                        .success(false)
                                        .message("AI service error: " + aiResponse.getErrorMessage())
                                        .build();
                }

                // Parse AI response
                double similarityScore = 0.5;
                List<String> overlappingKeywords = new ArrayList<>();
                String fitLevel = "MEDIUM";
                String explanation = "";

                try {
                        String content = aiResponse.getContent().trim();
                        if (content.startsWith("```")) {
                                content = content.replaceAll("```json\\n?", "").replaceAll("```\\n?", "");
                        }
                        Map<String, Object> parsed = objectMapper.readValue(content,
                                        new TypeReference<Map<String, Object>>() {
                                        });

                        if (parsed.get("similarityScore") != null) {
                                similarityScore = ((Number) parsed.get("similarityScore")).doubleValue();
                        }
                        overlappingKeywords = getStringList(parsed, "overlappingKeywords");
                        fitLevel = (String) parsed.getOrDefault("fitLevel", "MEDIUM");
                        explanation = (String) parsed.getOrDefault("explanation", "");
                } catch (Exception e) {
                        log.warn("Failed to parse AI response: {}", e.getMessage());
                }

                AIAuditLog auditLog = auditService.createAuditLog(
                                userId, request.getConferenceId(), AIFeature.SIMILARITY_HINT,
                                aiGateway.getCurrentModelName(), userPrompt, aiResponse.getContent(),
                                aiResponse.getProcessingTimeMs(), aiResponse.getTokensUsed());

                return SimilarityHintResponse.builder()
                                .success(true)
                                .message("Similarity calculated")
                                .similarityScore(similarityScore)
                                .overlappingKeywords(overlappingKeywords)
                                .fitLevel(fitLevel)
                                .explanation(explanation)
                                .auditLogId(auditLog.getId())
                                .processingTimeMs(aiResponse.getProcessingTimeMs())
                                .build();
        }

        @SuppressWarnings("unchecked")
        private List<String> getStringList(Map<String, Object> map, String key) {
                Object value = map.get(key);
                if (value instanceof List) {
                        return (List<String>) value;
                }
                return new ArrayList<>();
        }
}
