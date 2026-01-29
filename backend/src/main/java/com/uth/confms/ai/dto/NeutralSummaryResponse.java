package com.uth.confms.ai.dto;

import lombok.*;

/**
 * Response DTO cho tính năng tạo tóm tắt trung lập.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NeutralSummaryResponse {

    private boolean success;
    private String message;

    /** Tóm tắt trung lập (không chứa ngôn ngữ đánh giá) */
    private String summary;

    /** Số từ của summary */
    private Integer wordCount;

    /** ID của audit log để tracking */
    private Long auditLogId;

    /** Thời gian xử lý (ms) */
    private Long processingTimeMs;

    /** Lý giải của AI cho gợi ý này */
    private String rationale;
}
