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
public class RebuttalDTO {
    private Long id;
    private Long submissionId;
    private Long authorId;
    private String content;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
}

