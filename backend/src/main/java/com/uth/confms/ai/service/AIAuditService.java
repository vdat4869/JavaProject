package com.uth.confms.ai.service;

import com.uth.confms.ai.entity.AIAuditLog;
import com.uth.confms.ai.enums.AIFeature;
import com.uth.confms.ai.repository.AIAuditLogRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * Service quản lý audit log cho AI.
 * Ghi lại mọi lời gọi AI để đảm bảo tính minh bạch.
 */
@Service
public class AIAuditService {

    private final AIAuditLogRepository auditLogRepository;

    public AIAuditService(AIAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Tạo audit log cho một lời gọi AI.
     */
    public AIAuditLog createAuditLog(
            Long userId,
            Long conferenceId,
            AIFeature feature,
            String modelIdentifier,
            String input,
            String output,
            Long processingTimeMs,
            Integer tokensUsed) {

        AIAuditLog log = AIAuditLog.builder()
                .userId(userId)
                .conferenceId(conferenceId)
                .aiFeature(feature)
                .modelIdentifier(modelIdentifier)
                .timestamp(LocalDateTime.now())
                .inputHash(hashContent(input))
                .outputHash(hashContent(output))
                .processingTimeMs(processingTimeMs)
                .tokensUsed(tokensUsed)
                .build();

        return auditLogRepository.save(log);
    }

    /**
     * Tạo audit log khi có lỗi.
     */
    public AIAuditLog createErrorLog(
            Long userId,
            Long conferenceId,
            AIFeature feature,
            String modelIdentifier,
            String input,
            String errorMessage,
            Long processingTimeMs) {

        AIAuditLog log = AIAuditLog.builder()
                .userId(userId)
                .conferenceId(conferenceId)
                .aiFeature(feature)
                .modelIdentifier(modelIdentifier)
                .timestamp(LocalDateTime.now())
                .inputHash(hashContent(input))
                .outputHash("ERROR")
                .errorMessage(truncate(errorMessage, 500))
                .processingTimeMs(processingTimeMs)
                .build();

        return auditLogRepository.save(log);
    }

    /**
     * Cập nhật trạng thái user đã chấp nhận gợi ý.
     */
    public void updateUserAccepted(Long auditLogId, boolean accepted) {
        auditLogRepository.findById(auditLogId).ifPresent(log -> {
            log.setUserAccepted(accepted);
            auditLogRepository.save(log);
        });
    }

    /**
     * Hash nội dung để bảo mật (không lưu nội dung gốc).
     */
    private String hashContent(String content) {
        if (content == null || content.isBlank()) {
            return "EMPTY";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16); // Chỉ lấy 16 ký tự đầu
        } catch (NoSuchAlgorithmException e) {
            return "HASH_ERROR";
        }
    }

    /**
     * Cắt ngắn chuỗi nếu vượt quá độ dài cho phép.
     */
    private String truncate(String str, int maxLength) {
        if (str == null)
            return null;
        return str.length() <= maxLength ? str : str.substring(0, maxLength);
    }
}
