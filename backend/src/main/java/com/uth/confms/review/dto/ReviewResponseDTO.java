package com.uth.confms.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private Long id;
    private Long assignmentId;
    private Long submissionId;
    private Long reviewerId;
    private String reviewerName; // Only visible to chair/admin, null for double-blind
    private String summary;
    private String strengths;
    private String weaknesses;
    private String comments;
    private String score;
    private String status;
    private Boolean isConfidential;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
}

