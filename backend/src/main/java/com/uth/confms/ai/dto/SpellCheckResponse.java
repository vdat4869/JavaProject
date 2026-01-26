package com.uth.confms.ai.dto;

import lombok.*;
import java.util.List;

/**
 * Response DTO cho tính năng kiểm tra chính tả.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpellCheckResponse {

    private boolean success;
    private String message;

    /** Danh sách các gợi ý sửa lỗi */
    private List<Suggestion> suggestions;

    /** ID của audit log để tracking */
    private Long auditLogId;

    /** Thời gian xử lý (ms) */
    private Long processingTimeMs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Suggestion {
        /** Loại lỗi: SPELLING, GRAMMAR, STYLE */
        private String type;

        /** Văn bản gốc có lỗi */
        private String original;

        /** Gợi ý sửa */
        private String replacement;

        /** Vị trí trong văn bản (nếu có) */
        private Integer startIndex;
        private Integer endIndex;

        /** Giải thích tại sao cần sửa */
        private String explanation;

        /** Trường nào: title, abstract, keywords */
        private String field;
    }
}
