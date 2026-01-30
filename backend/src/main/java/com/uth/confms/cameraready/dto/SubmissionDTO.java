package com.uth.confms.cameraready.dto;

import com.uth.confms.cameraready.entity.CameraReadyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO cho thông tin bài nộp camera-ready.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDTO {

    private UUID id;
    private Long paperId;
    private String paperTitle;
    private Long conferenceId;
    private Long trackId;
    private String trackName;
    private CameraReadyStatus status; // Trạng thái hiện tại
    private VersionDTO currentVersion; // Phiên bản hiện tại
    private Boolean copyrightConfirmed; // Đã xong bản quyền chưa
    private LocalDateTime copyrightConfirmedAt; // Thời gian xác nhận bản quyền
    private LocalDateTime deadline; // Hạn chót
    private Boolean canUpload; // Có được phép upload không
    private Boolean canConfirmCopyright; // Có được phép xác nhận bản quyền không
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
