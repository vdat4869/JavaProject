package com.uth.confms.cameraready.dto;

import com.uth.confms.cameraready.entity.CameraReadyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO cho danh sách bài nộp (dùng trong Chair view).
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionListDTO {

    private UUID id;
    private Long paperId;
    private String paperTitle; // Tiêu đề bài báo
    private Long trackId;
    private String trackName;
    private CameraReadyStatus status; // Trạng thái
    private Integer currentVersionNumber; // Số phiên bản hiện tại
    private Boolean copyrightConfirmed; // Trạng thái bản quyền
    private VersionDTO.UserDTO correspondingAuthor; // Tác giả liên hệ
    private LocalDateTime updatedAt;
}
