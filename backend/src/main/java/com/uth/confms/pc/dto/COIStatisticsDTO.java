package com.uth.confms.pc.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO đại diện cho COI statistics của một conference
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class COIStatisticsDTO {
  private Long conferenceId;
  private String conferenceName;

  // Overall statistics
  private int totalCOIs;
  private int activeCOIs;
  private int inactiveCOIs;

  // COI distribution by type
  private Map<String, Long> coiByType; // CO_AUTHOR, COLLABORATOR, ADVISOR, INSTITUTIONAL, OTHER

  // COI distribution by reviewer
  private int reviewersWithCOIs;
  private int submissionsWithCOIs;

  // Recent COIs (last 30 days)
  private int recentCOIs;
}
