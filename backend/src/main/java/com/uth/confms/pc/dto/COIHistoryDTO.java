package com.uth.confms.pc.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO đại diện cho COI history entry
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class COIHistoryDTO {
  private Long id;
  private Long reviewerId;
  private String reviewerEmail;
  private String reviewerName;
  private Long submissionId;
  private String submissionTitle;
  private String coiType;
  private String reason;
  private Boolean active;
  private LocalDateTime declaredAt;
  private String action; // DECLARED, REMOVED, AUTO_DETECTED
}
