package com.uth.confms.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho SLA (Service Level Agreement) statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SLAStatsDTO {
    private Long conferenceId;
    private String conferenceName;
    
    // Overall SLA metrics
    private Integer totalAssignments;
    private Integer completedOnTime;
    private Integer completedLate;
    private Integer pendingPastDeadline;
    private Double onTimeCompletionRate; // Percentage
    
    // Deadline information
    private java.time.LocalDateTime reviewDeadline;
    private Boolean hasDeadline;
    
    // Violations
    private List<ViolationDTO> violations;
    
    // Per-reviewer SLA
    private List<ReviewerSLADTO> reviewerSLAs;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViolationDTO {
        private Long assignmentId;
        private Long submissionId;
        private Long reviewerId;
        private String reviewerName;
        private java.time.LocalDateTime deadline;
        private java.time.LocalDateTime submittedAt;
        private Long daysLate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewerSLADTO {
        private Long reviewerId;
        private String reviewerName;
        private Integer totalAssignments;
        private Integer onTimeCount;
        private Integer lateCount;
        private Double onTimeRate;
    }
}
