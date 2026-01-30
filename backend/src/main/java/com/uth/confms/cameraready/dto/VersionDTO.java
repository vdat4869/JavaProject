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
    private Integer versionNumber; // Số thứ tự phiên bản
    private String originalFilename; // Tên file gốc
    private Long fileSizeBytes; // Kích thước file
    private String checksumSha256; // Checksum
    private Integer pageCount; // Số trang
    private String pageSize; // Kích thước trang
    private ValidationResultDTO validationResult; // Kết quả kiểm tra
    private Boolean validationPassed; // Đã qua kiểm tra chưa
    private UserDTO uploadedBy; // Người upload
    private LocalDateTime uploadedAt; // Thời gian upload
    private Boolean isCurrent; // Có phải phiên bản hiện tại không

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        private Long id;
        private String fullName;
        private String email;
    }
}
