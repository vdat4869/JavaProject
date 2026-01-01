package com.uth.confms.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponseDTO {
    private Long id;
    private Long submissionId;
    private String submissionTitle;
    private Long reviewerId;
    private String reviewerEmail;
    private String reviewerName;
    private String status;
    private Boolean isPrimary;
    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;
}

