package com.uth.confms.pc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO đại diện cho workload của một reviewer
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkloadDTO {
  private Long reviewerId;
  private String reviewerEmail;
  private String reviewerName;
  private Long conferenceId;
  private String conferenceName;

  // Assignment counts by status
  private long totalAssignments;
  private long assignedCount;
  private long acceptedCount;
  private long declinedCount;
  private long completedCount;

  // Workload status
  private String workloadStatus; // LOW, NORMAL, HIGH, OVERLOADED
  private int maxAssignments;
  private double workloadPercentage; // Percentage of max assignments used
}
