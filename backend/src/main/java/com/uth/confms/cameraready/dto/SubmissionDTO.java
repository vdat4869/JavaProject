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
    private CameraReadyStatus status;
    private VersionDTO currentVersion;
    private Boolean copyrightConfirmed;
    private LocalDateTime copyrightConfirmedAt;
    private LocalDateTime deadline;
    private Boolean canUpload;
    private Boolean canConfirmCopyright;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
