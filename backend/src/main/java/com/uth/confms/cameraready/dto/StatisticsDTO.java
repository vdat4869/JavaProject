package com.uth.confms.cameraready.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO cho thống kê camera-ready.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDTO {

    private UUID conferenceId;
    private LocalDateTime deadline;
    private Integer daysRemaining;
    private Integer totalAcceptedPapers;
    private StatusStatistics statistics;
    private List<TrackStatistics> byTrack;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusStatistics {
        private Map<String, Long> byStatus;
        private Long copyrightConfirmed;
        private Long copyrightPending;
        private Double submissionRate;
        private Double approvalRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackStatistics {
        private UUID trackId;
        private String trackName;
        private Integer total;
        private Integer submitted;
        private Integer approved;
    }
}
