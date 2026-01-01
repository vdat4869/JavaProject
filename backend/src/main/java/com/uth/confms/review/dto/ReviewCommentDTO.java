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
public class ReviewCommentDTO {
    private Long id;
    private Long submissionId;
    private Long reviewerId;
    private String reviewerName; // Only visible to chair/admin for internal comments
    private String content;
    private Boolean isInternal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

