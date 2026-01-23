package com.uth.confms.pc.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO đại diện cho workload statistics của một conference
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkloadStatsDTO {
  private Long conferenceId;
  private String conferenceName;

  // Overall statistics
  private int totalReviewers;
  private int totalAssignments;
  private double averageAssignmentsPerReviewer;

  // Workload distribution
  private int lowWorkloadCount; // < 3 assignments
  private int normalWorkloadCount; // 3-5 assignments
  private int highWorkloadCount; // 6-8 assignments
  private int overloadedCount; // > 8 assignments

  // Status distribution
  private int assignedCount;
  private int acceptedCount;
  private int declinedCount;
  private int completedCount;

  // Reviewer workload details
  private List<WorkloadDTO> reviewerWorkloads;
}
