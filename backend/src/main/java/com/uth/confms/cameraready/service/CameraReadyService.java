package com.uth.confms.cameraready.service;

import com.uth.confms.cameraready.dto.*;
import com.uth.confms.cameraready.entity.CameraReadyStatus;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Service interface cho quản lý camera-ready submissions.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
public interface CameraReadyService {

        // ==================== Author Operations ====================

        /**
         * Lấy thông tin submission camera-ready cho một bài báo.
         */
        SubmissionDTO getSubmissionByPaperId(Long conferenceId, Long paperId);

        /**
         * Tải lên phiên bản camera-ready mới (PDF).
         * Kiểm tra deadline và quyền tác giả trước khi upload.
         */
        VersionDTO uploadVersion(Long conferenceId, Long paperId, MultipartFile file, Long uploaderId);

        List<VersionDTO> listVersions(Long conferenceId, Long paperId);

        /**
         * Tải xuống file PDF của một phiên bản cụ thể.
         */
        Resource downloadVersion(Long conferenceId, Long paperId, UUID versionId);

        String getVersionFilename(UUID versionId);

        /**
         * Xác nhận bản quyền cho bài báo.
         */
        SubmissionDTO confirmCopyright(Long conferenceId, Long paperId, CopyrightConfirmRequestDTO request,
                        Long userId);

        // ==================== Chair Operations ====================

        /**
         * Lấy danh sách camera-ready submissions (cho Chair).
         * Hỗ trợ lọc theo track, trạng thái, và trạng thái bản quyền.
         */
        Page<SubmissionListDTO> listSubmissions(Long conferenceId, Long trackId,
                        CameraReadyStatus status, Boolean copyrightConfirmed,
                        Pageable pageable);

        /**
         * Duyệt bài nộp camera-ready (Approve/Reject).
         */
        ReviewResponseDTO reviewSubmission(Long conferenceId, UUID submissionId,
                        ReviewRequestDTO request, Long reviewerId);

        /**
         * Chọn phiên bản hiện tại (Current Version) cho submission.
         */
        SubmissionDTO setCurrentVersion(Long conferenceId, UUID submissionId, UUID versionId, Long userId);

        MetadataDTO getMetadata(UUID submissionId);

        /**
         * Cập nhật metadata (DOI, số trang, v.v.) cho bài báo.
         */
        MetadataDTO updateMetadata(UUID submissionId, MetadataUpdateRequestDTO request, Long userId);

        StatisticsDTO getStatistics(Long conferenceId);

        /**
         * Xuất dữ liệu kỷ yếu dưới dạng JSON.
         */
        ProceedingsExportDTO exportProceedingsJson(Long conferenceId, Long trackId, CameraReadyStatus status);

        byte[] exportProceedingsCsv(Long conferenceId, Long trackId, CameraReadyStatus status);

        byte[] exportProceedingsZip(Long conferenceId, Long trackId, CameraReadyStatus status);

        byte[] exportProceedingsPdf(Long conferenceId, Long trackId, CameraReadyStatus status);

        // ==================== Admin Operations ====================

        /**
         * Mở đợt nộp camera-ready cho conference.
         */
        int openCameraReady(Long conferenceId, java.time.LocalDateTime deadline, Long userId);

        /**
         * Đóng đợt nộp camera-ready.
         */
        void closeCameraReady(Long conferenceId, String reason, Long userId);
}
