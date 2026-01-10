package com.uth.confms.cameraready.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO cho thông tin phiên bản camera-ready.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionDTO {

    private UUID id;
    private UUID submissionId;
    private Integer versionNumber;
    private String originalFilename;
    private Long fileSizeBytes;
    private String checksumSha256;
    private Integer pageCount;
    private String pageSize;
    private ValidationResultDTO validationResult;
    private Boolean validationPassed;
    private UserDTO uploadedBy;
    private LocalDateTime uploadedAt;
    private Boolean isCurrent;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        private UUID id;
        private String fullName;
        private String email;
    }
}
