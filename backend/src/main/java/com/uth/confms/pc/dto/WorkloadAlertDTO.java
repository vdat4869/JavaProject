package com.uth.confms.pc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO đại diện cho workload alert (overloaded reviewers)
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkloadAlertDTO {
  private Long reviewerId;
  private String reviewerEmail;
  private String reviewerName;
  private Long conferenceId;
  private String conferenceName;
  private long currentAssignments;
  private int maxAssignments;
  private double workloadPercentage;
  private String alertType; // OVERLOADED, NEAR_LIMIT
  private String message;
}
