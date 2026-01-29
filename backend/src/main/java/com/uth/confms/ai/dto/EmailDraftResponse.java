package com.uth.confms.ai.dto;

import lombok.*;

/**
 * Response DTO cho tính năng soạn email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailDraftResponse {

    private boolean success;
    private String message;

    /** Tiêu đề email */
    private String subject;

    /** Nội dung email (HTML hoặc plain text) */
    private String body;

    /** ID của audit log để tracking */
    private Long auditLogId;

    /** Thời gian xử lý (ms) */
    private Long processingTimeMs;

    /** Lý giải của AI cho gợi ý này */
    private String rationale;
}
