package com.uth.confms.cameraready.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho xuất kỷ yếu (proceedings).
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProceedingsExportDTO {

    private Long conferenceId;
    private String conferenceName; // Tên hội nghị
    private LocalDateTime exportedAt; // Thời gian xuất
    private Integer totalPapers; // Tổng số bài báo
    private List<PaperExportDTO> papers; // Danh sách bài báo

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaperExportDTO {
        private Long paperId;
        private String title; // Tiêu đề bài báo
        private String abstractText; // Tóm tắt
        private List<String> keywords; // Từ khóa
        private List<AuthorExportDTO> authors; // Danh sách tác giả
        private TrackExportDTO track; // Track
        private String doi;
        private Integer startPage;
        private Integer endPage;
        private String pdfPath; // Đường dẫn file PDF
        private PresentationExportDTO presentation; // Thông tin trình bày
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorExportDTO {
        private String name;
        private String email;
        private String affiliation;
        private Boolean isCorresponding;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackExportDTO {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PresentationExportDTO {
        private String type;
        private Integer durationMinutes;
    }
}
