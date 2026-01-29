package com.uth.confms.ai.dto;

import lombok.*;
import java.util.List;

/**
 * Response DTO cho tính năng gợi ý độ tương đồng reviewer-paper.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimilarityHintResponse {

    private boolean success;
    private String message;

    /** Điểm tương đồng (0-1) */
    private Double similarityScore;

    /** Các keywords/topics trùng lặp */
    private List<String> overlappingKeywords;

    /** Giải thích về độ tương đồng */
    private String explanation;

    /** Mức độ phù hợp: HIGH, MEDIUM, LOW */
    private String fitLevel;

    /** ID của audit log để tracking */
    private Long auditLogId;

    /** Thời gian xử lý (ms) */
    private Long processingTimeMs;

    /** Lý giải của AI cho gợi ý này */
    private String rationale;
}
