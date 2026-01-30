package com.uth.confms.cameraready.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    private Long conferenceId;
    private LocalDateTime deadline; // Hạn chót
    private Integer daysRemaining; // Số ngày còn lại
    private Integer totalAcceptedPapers; // Tổng số bài được chấp nhận
    private StatusStatistics statistics; // Thống kê theo trạng thái
    private List<TrackStatistics> byTrack; // Thống kê theo track

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusStatistics {
        private Map<String, Long> byStatus; // Số lượng theo từng trạng thái
        private Long copyrightConfirmed; // Số lượng đã xác nhận bản quyền
        private Long copyrightPending; // Số lượng chưa xác nhận bản quyền
        private Double submissionRate; // Tỷ lệ đã nộp
        private Double approvalRate; // Tỷ lệ đã duyệt
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackStatistics {
        private Long trackId;
        private String trackName;
        private Integer total; // Tổng số bài trong track
        private Integer submitted; // Số bài đã nộp camera-ready
        private Integer approved; // Số bài đã duyệt
    }
}
