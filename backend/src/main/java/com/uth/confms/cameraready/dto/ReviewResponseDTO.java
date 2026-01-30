package com.uth.confms.cameraready.dto;

import com.uth.confms.cameraready.entity.CameraReadyStatus;
import com.uth.confms.cameraready.entity.ReviewDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO cho response duyệt bài nộp.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {

    private UUID reviewId;
    private UUID submissionId;
    private ReviewDecision decision; // Quyết định
    private String note; // Ghi chú
    private CameraReadyStatus newStatus; // Trạng thái mới của submission
    private VersionDTO.UserDTO reviewedBy; // Người duyệt
    private LocalDateTime reviewedAt; // Thời gian duyệt
}
