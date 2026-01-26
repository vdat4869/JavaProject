package com.uth.confms.ai.dto;

import lombok.*;
import java.util.List;

/**
 * Response DTO cho tính năng polish abstract.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbstractPolishResponse {

    private boolean success;
    private String message;

    /** Abstract gốc */
    private String originalAbstract;

    /** Abstract đã được cải thiện */
    private String polishedAbstract;

    /** Danh sách các thay đổi được giải thích */
    private List<Change> changes;

    /** ID của audit log để tracking */
    private Long auditLogId;

    /** Thời gian xử lý (ms) */
    private Long processingTimeMs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Change {
        /** Văn bản gốc */
        private String before;

        /** Văn bản mới */
        private String after;

        /** Loại thay đổi: CLARITY, GRAMMAR, CONCISENESS, FLOW */
        private String changeType;

        /** Giải thích tại sao thay đổi */
        private String explanation;
    }
}
