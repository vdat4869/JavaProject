package com.uth.confms.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uth.confms.ai.dto.*;
import com.uth.confms.ai.entity.AIAuditLog;
import com.uth.confms.ai.enums.AIFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service xử lý các tính năng AI cho Chair.
 * Bao gồm: Email Drafting.
 */
@Service
public class ChairAIService {

    private static final Logger log = LoggerFactory.getLogger(ChairAIService.class);

    private final AIGatewayService aiGateway;
    private final AIAuditService auditService;
    private final ObjectMapper objectMapper;

    public ChairAIService(AIGatewayService aiGateway, AIAuditService auditService) {
        this.aiGateway = aiGateway;
        this.auditService = auditService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Soạn thảo bản nháp email thông báo (Decision, Invitation, Reminder, etc.)
     * Dựa trên ngữ cảnh và tông giọng yêu cầu.
     * QUAN TRỌNG: AI chỉ soạn bản nháp, Chair phải kiểm tra lại trước khi gửi.
     */
    public EmailDraftResponse draftEmail(EmailDraftRequest request, Long userId) {
        String languageInstruction = "vi".equals(request.getLanguage())
                ? "Write in Vietnamese."
                : "Write in English.";

        String toneInstruction = switch (request.getTone()) {
            case "friendly" -> "Use a friendly but professional tone.";
            case "urgent" -> "Use an urgent but polite tone.";
            default -> "Use a formal academic tone.";
        };

        String systemPrompt = String.format("""
                You are an academic conference email writer.
                %s
                %s

                Write a professional email for the following purpose.

                Return a JSON object with:
                - subject: the email subject line
                - body: the email body (use line breaks for paragraphs)

                Email types:
                - DECISION: Notify author of paper acceptance/rejection
                - REMINDER: Remind about upcoming deadline
                - INVITATION: Invite someone to review/participate
                - NOTIFICATION: General conference notification

                Only return valid JSON, no other text.
                """, languageInstruction, toneInstruction);

        String userPrompt = String.format("""
                Email type: %s
                Conference: %s
                Recipient: %s
                Context: %s
                """,
                request.getEmailType(),
                request.getConferenceName() != null ? request.getConferenceName() : "Academic Conference",
                request.getRecipientName(),
                request.getContext() != null ? request.getContext() : "General notification");

        AIGatewayService.AIResponse aiResponse = aiGateway.chat(systemPrompt, userPrompt);

        if (!aiResponse.isSuccess()) {
            auditService.createErrorLog(userId, request.getConferenceId(), AIFeature.EMAIL_DRAFT,
                    aiGateway.getCurrentModelName(), userPrompt, aiResponse.getErrorMessage(),
                    aiResponse.getProcessingTimeMs());

            return EmailDraftResponse.builder()
                    .success(false)
                    .message("AI service error: " + aiResponse.getErrorMessage())
                    .build();
        }

        // Parse AI response
        String subject = "";
        String body = "";

        try {
            String content = aiResponse.getContent().trim();
            if (content.startsWith("```")) {
                content = content.replaceAll("```json\\n?", "").replaceAll("```\\n?", "");
            }
            Map<String, Object> parsed = objectMapper.readValue(content,
                    new TypeReference<Map<String, Object>>() {
                    });

            subject = (String) parsed.getOrDefault("subject", "");
            body = (String) parsed.getOrDefault("body", "");
        } catch (Exception e) {
            log.warn("Failed to parse AI response: {}", e.getMessage());
            // Fallback: use entire response as body
            body = aiResponse.getContent();
        }

        AIAuditLog auditLog = auditService.createAuditLog(
                userId, request.getConferenceId(), AIFeature.EMAIL_DRAFT,
                aiGateway.getCurrentModelName(), userPrompt, aiResponse.getContent(),
                aiResponse.getProcessingTimeMs(), aiResponse.getTokensUsed());

        return EmailDraftResponse.builder()
                .success(true)
                .message("Email draft generated. Please review before sending.")
                .subject(subject)
                .body(body)
                .auditLogId(auditLog.getId())
                .processingTimeMs(aiResponse.getProcessingTimeMs())
                .build();
    }
}
