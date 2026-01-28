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
    private String paperTitle;
    private Long trackId;
    private String trackName;
    private CameraReadyStatus status;
    private Integer currentVersionNumber;
    private Boolean copyrightConfirmed;
    private VersionDTO.UserDTO correspondingAuthor;
    private LocalDateTime updatedAt;
}
