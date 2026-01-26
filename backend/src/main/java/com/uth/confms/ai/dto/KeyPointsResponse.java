package com.uth.confms.ai.dto;

import lombok.*;
import java.util.List;

/**
 * Response DTO cho tính năng trích xuất key points.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeyPointsResponse {

    private boolean success;
    private String message;

    /** Các claims/contributions chính của paper */
    private List<String> claims;

    /** Các methods/approaches được đề cập */
    private List<String> methods;

    /** Các datasets được sử dụng (nếu có) */
    private List<String> datasets;

    /** Các kết quả/findings chính */
    private List<String> findings;

    /** ID của audit log để tracking */
    private Long auditLogId;

    /** Thời gian xử lý (ms) */
    private Long processingTimeMs;
}
