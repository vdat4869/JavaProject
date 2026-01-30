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
 * Service xử lý các tính năng AI cho Author.
 * Bao gồm: Spell Check, Abstract Polish, Keyword Suggest.
 */
@Service
public class AuthorAIService {

    private static final Logger log = LoggerFactory.getLogger(AuthorAIService.class);

    private final AIGatewayService aiGateway;
    private final AIAuditService auditService;
    private final ObjectMapper objectMapper;

    public AuthorAIService(AIGatewayService aiGateway, AIAuditService auditService) {
        this.aiGateway = aiGateway;
        this.auditService = auditService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Kiểm tra chính tả và ngữ pháp.
     */
    public SpellCheckResponse spellCheck(SpellCheckRequest request, Long userId) {
        String systemPrompt = """
                You are a professional academic editor. Check the following academic text for:
                1. Spelling errors
                2. Grammar mistakes
                3. Style issues (for academic writing)

                Return a JSON array of suggestions. Each suggestion should have:
                - type: "SPELLING", "GRAMMAR", or "STYLE"
                - original: the problematic text
                - replacement: the suggested correction
                - explanation: why this change is needed
                - field: which field the error is in ("title", "abstract", or "keywords")

                If the text is perfect, return an empty array: []
                Only return valid JSON, no other text.
                """;

        StringBuilder userPrompt = new StringBuilder();
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            userPrompt.append("Title: ").append(request.getTitle()).append("\n\n");
        }
        userPrompt.append("Abstract: ").append(request.getAbstractText());
        if (request.getKeywords() != null && !request.getKeywords().isBlank()) {
            userPrompt.append("\n\nKeywords: ").append(request.getKeywords());
        }

        AIGatewayService.AIResponse aiResponse = aiGateway.chat(systemPrompt, userPrompt.toString());

        if (!aiResponse.isSuccess()) {
            auditService.createErrorLog(userId, request.getConferenceId(), AIFeature.SPELL_CHECK,
                    aiGateway.getCurrentModelName(), userPrompt.toString(), aiResponse.getErrorMessage(),
                    aiResponse.getProcessingTimeMs());

            return SpellCheckResponse.builder()
                    .success(false)
                    .message("AI service error: " + aiResponse.getErrorMessage())
                    .build();
        }

        // Parse AI response
        List<SpellCheckResponse.Suggestion> suggestions = new ArrayList<>();
        try {
            String content = aiResponse.getContent().trim();
            // Remove markdown code blocks if present
            if (content.startsWith("```")) {
                content = content.replaceAll("```json\\n?", "").replaceAll("```\\n?", "");
            }
            List<Map<String, Object>> parsed = objectMapper.readValue(content,
                    new TypeReference<List<Map<String, Object>>>() {
                    });

            for (Map<String, Object> item : parsed) {
                suggestions.add(SpellCheckResponse.Suggestion.builder()
                        .type((String) item.get("type"))
                        .original((String) item.get("original"))
                        .replacement((String) item.get("replacement"))
                        .explanation((String) item.get("explanation"))
                        .field((String) item.get("field"))
                        .build());
            }
        } catch (Exception e) {
            log.warn("Failed to parse AI response: {}", e.getMessage());
        }

        // Audit log
        AIAuditLog auditLog = auditService.createAuditLog(
                userId, request.getConferenceId(), AIFeature.SPELL_CHECK,
                aiGateway.getCurrentModelName(), userPrompt.toString(), aiResponse.getContent(),
                aiResponse.getProcessingTimeMs(), aiResponse.getTokensUsed());

        return SpellCheckResponse.builder()
                .success(true)
                .message(suggestions.isEmpty() ? "No issues found!" : "Found " + suggestions.size() + " suggestions")
                .suggestions(suggestions)
                .auditLogId(auditLog.getId())
                .processingTimeMs(aiResponse.getProcessingTimeMs())
                .build();
    }

    /**
     * Cải thiện nội dung abstract (Abstract Polish).
     * AI sẽ gợi ý bản sửa lỗi và giải thích các thay đổi về độ rõ ràng, ngữ pháp,
     * ngắn gọn.
     */
    public AbstractPolishResponse polishAbstract(AbstractPolishRequest request, Long userId) {
        String systemPrompt = """
                You are a professional academic editor. Improve the following abstract while:
                1. Maintaining the original meaning and key information
                2. Improving clarity and readability
                3. Using formal academic language
                4. Ensuring logical flow

                Return a JSON object with:
                - polishedAbstract: the improved abstract
                - changes: array of {before, after, changeType, explanation}
                  where changeType is: "CLARITY", "GRAMMAR", "CONCISENESS", or "FLOW"

                Only return valid JSON, no other text.
                """;

        String userPrompt = "Abstract to polish:\n\n" + request.getAbstractText();

        AIGatewayService.AIResponse aiResponse = aiGateway.chat(systemPrompt, userPrompt);

        if (!aiResponse.isSuccess()) {
            auditService.createErrorLog(userId, request.getConferenceId(), AIFeature.ABSTRACT_POLISH,
                    aiGateway.getCurrentModelName(), userPrompt, aiResponse.getErrorMessage(),
                    aiResponse.getProcessingTimeMs());

            return AbstractPolishResponse.builder()
                    .success(false)
                    .message("AI service error: " + aiResponse.getErrorMessage())
                    .build();
        }

        // Parse AI response
        String polishedAbstract = request.getAbstractText();
        List<AbstractPolishResponse.Change> changes = new ArrayList<>();

        try {
            String content = aiResponse.getContent().trim();
            if (content.startsWith("```")) {
                content = content.replaceAll("```json\\n?", "").replaceAll("```\\n?", "");
            }
            Map<String, Object> parsed = objectMapper.readValue(content,
                    new TypeReference<Map<String, Object>>() {
                    });

            polishedAbstract = (String) parsed.get("polishedAbstract");

            @SuppressWarnings("unchecked")
            List<Map<String, String>> changesList = (List<Map<String, String>>) parsed.get("changes");
            if (changesList != null) {
                for (Map<String, String> item : changesList) {
                    changes.add(AbstractPolishResponse.Change.builder()
                            .before(item.get("before"))
                            .after(item.get("after"))
                            .changeType(item.get("changeType"))
                            .explanation(item.get("explanation"))
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse AI abstract polishing response: {}. Content: {}", e.getMessage(),
                    aiResponse.getContent());
            polishedAbstract = aiResponse.getContent();
        }

        AIAuditLog auditLog = auditService.createAuditLog(
                userId, request.getConferenceId(), AIFeature.ABSTRACT_POLISH,
                aiGateway.getCurrentModelName(), userPrompt, aiResponse.getContent(),
                aiResponse.getProcessingTimeMs(), aiResponse.getTokensUsed());

        return AbstractPolishResponse.builder()
                .success(true)
                .message("Abstract polished successfully")
                .originalAbstract(request.getAbstractText())
                .polishedAbstract(polishedAbstract)
                .changes(changes)
                .auditLogId(auditLog.getId())
                .processingTimeMs(aiResponse.getProcessingTimeMs())
                .build();
    }

    /**
     * Gợi ý từ khóa (Keywords) dựa trên tiêu đề và abstract.
     * Hỗ trợ tránh các từ khóa đã có sẵn.
     */
    public KeywordSuggestResponse suggestKeywords(KeywordSuggestRequest request, Long userId) {
        String systemPrompt = """
                You are an expert academic keyword generator. Suggest relevant keywords for the following paper.

                Return a JSON array of keyword suggestions. Each suggestion should have:
                - keyword: the suggested keyword or phrase
                - relevanceScore: 0.0 to 1.0 indicating how relevant it is
                - explanation: why this keyword fits the paper
                - isCommon: true if this is a commonly used term in the field

                Consider:
                1. Main topics and themes
                2. Methodologies mentioned
                3. Key concepts
                4. Application domains

                Avoid keywords that are already provided.
                Only return valid JSON, no other text.
                """;

        StringBuilder userPrompt = new StringBuilder();
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            userPrompt.append("Title: ").append(request.getTitle()).append("\n\n");
        }
        userPrompt.append("Abstract: ").append(request.getAbstractText());
        if (request.getExistingKeywords() != null && !request.getExistingKeywords().isBlank()) {
            userPrompt.append("\n\nExisting keywords (avoid these): ").append(request.getExistingKeywords());
        }
        userPrompt.append("\n\nSuggest up to ").append(request.getMaxKeywords() != null ? request.getMaxKeywords() : 5)
                .append(" keywords.");

        AIGatewayService.AIResponse aiResponse = aiGateway.chat(systemPrompt, userPrompt.toString());

        if (!aiResponse.isSuccess()) {
            auditService.createErrorLog(userId, request.getConferenceId(), AIFeature.KEYWORD_SUGGEST,
                    aiGateway.getCurrentModelName(), userPrompt.toString(), aiResponse.getErrorMessage(),
                    aiResponse.getProcessingTimeMs());

            return KeywordSuggestResponse.builder()
                    .success(false)
                    .message("AI service error: " + aiResponse.getErrorMessage())
                    .build();
        }

        // Parse AI response
        List<KeywordSuggestResponse.KeywordSuggestion> keywords = new ArrayList<>();
        try {
            String content = aiResponse.getContent().trim();
            if (content.startsWith("```")) {
                content = content.replaceAll("```json\\n?", "").replaceAll("```\\n?", "");
            }
            List<Map<String, Object>> parsed = objectMapper.readValue(content,
                    new TypeReference<List<Map<String, Object>>>() {
                    });

            for (Map<String, Object> item : parsed) {
                keywords.add(KeywordSuggestResponse.KeywordSuggestion.builder()
                        .keyword((String) item.get("keyword"))
                        .relevanceScore(
                                item.get("relevanceScore") != null ? ((Number) item.get("relevanceScore")).doubleValue()
                                        : 0.5)
                        .explanation((String) item.get("explanation"))
                        .isCommon(item.get("isCommon") != null ? (Boolean) item.get("isCommon") : false)
                        .build());
            }
        } catch (Exception e) {
            log.warn("Failed to parse AI response: {}", e.getMessage());
        }

        AIAuditLog auditLog = auditService.createAuditLog(
                userId, request.getConferenceId(), AIFeature.KEYWORD_SUGGEST,
                aiGateway.getCurrentModelName(), userPrompt.toString(), aiResponse.getContent(),
                aiResponse.getProcessingTimeMs(), aiResponse.getTokensUsed());

        return KeywordSuggestResponse.builder()
                .success(true)
                .message("Generated " + keywords.size() + " keyword suggestions")
                .keywords(keywords)
                .auditLogId(auditLog.getId())
                .processingTimeMs(aiResponse.getProcessingTimeMs())
                .build();
    }
}
