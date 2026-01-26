package com.uth.confms.ai.dto;

import lombok.*;
import java.util.List;

/**
 * Response DTO cho tính năng gợi ý keywords.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeywordSuggestResponse {

    private boolean success;
    private String message;

    /** Danh sách keywords được gợi ý */
    private List<KeywordSuggestion> keywords;

    /** ID của audit log để tracking */
    private Long auditLogId;

    /** Thời gian xử lý (ms) */
    private Long processingTimeMs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KeywordSuggestion {
        /** Keyword được gợi ý */
        private String keyword;

        /** Điểm liên quan (0-1) */
        private Double relevanceScore;

        /** Giải thích tại sao keyword này phù hợp */
        private String explanation;

        /** Có phải keyword phổ biến trong lĩnh vực không */
        private Boolean isCommon;
    }
}
