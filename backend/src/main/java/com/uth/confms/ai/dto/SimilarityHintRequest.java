package com.uth.confms.ai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

/**
 * Request DTO cho tính năng gợi ý độ tương đồng reviewer-paper.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimilarityHintRequest {

    private Long conferenceId;

    /** ID của submission */
    @NotNull(message = "Submission ID is required")
    private Long submissionId;

    /** Keywords/topics của paper */
    private List<String> paperKeywords;

    /** Topics của paper */
    private List<String> paperTopics;

    /** ID của reviewer để kiểm tra */
    @NotNull(message = "Reviewer ID is required")
    private Long reviewerId;

    /** Expertise đã khai báo của reviewer */
    private List<String> reviewerExpertise;
}
