package com.uth.confms.cameraready.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    private UUID conferenceId;
    private String conferenceName;
    private LocalDateTime exportedAt;
    private Integer totalPapers;
    private List<PaperExportDTO> papers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaperExportDTO {
        private UUID paperId;
        private String title;
        private String abstractText;
        private List<String> keywords;
        private List<AuthorExportDTO> authors;
        private TrackExportDTO track;
        private String doi;
        private Integer startPage;
        private Integer endPage;
        private String pdfPath;
        private PresentationExportDTO presentation;
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
        private UUID id;
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
