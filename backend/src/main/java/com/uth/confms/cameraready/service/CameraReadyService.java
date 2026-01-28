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

        SubmissionDTO getSubmissionByPaperId(Long conferenceId, Long paperId);

        VersionDTO uploadVersion(Long conferenceId, Long paperId, MultipartFile file, Long uploaderId);

        List<VersionDTO> listVersions(Long conferenceId, Long paperId);

        Resource downloadVersion(Long conferenceId, Long paperId, UUID versionId);

        String getVersionFilename(UUID versionId);

        SubmissionDTO confirmCopyright(Long conferenceId, Long paperId, CopyrightConfirmRequestDTO request,
                        Long userId);

        // ==================== Chair Operations ====================

        Page<SubmissionListDTO> listSubmissions(Long conferenceId, Long trackId,
                        CameraReadyStatus status, Boolean copyrightConfirmed,
                        Pageable pageable);

        ReviewResponseDTO reviewSubmission(Long conferenceId, UUID submissionId,
                        ReviewRequestDTO request, Long reviewerId);

        SubmissionDTO setCurrentVersion(Long conferenceId, UUID submissionId, UUID versionId, Long userId);

        MetadataDTO getMetadata(UUID submissionId);

        MetadataDTO updateMetadata(UUID submissionId, MetadataUpdateRequestDTO request, Long userId);

        StatisticsDTO getStatistics(Long conferenceId);

        ProceedingsExportDTO exportProceedingsJson(Long conferenceId, Long trackId, CameraReadyStatus status);

        byte[] exportProceedingsCsv(Long conferenceId, Long trackId, CameraReadyStatus status);

        byte[] exportProceedingsZip(Long conferenceId, Long trackId, CameraReadyStatus status);

        byte[] exportProceedingsPdf(Long conferenceId, Long trackId, CameraReadyStatus status);

        // ==================== Admin Operations ====================

        int openCameraReady(Long conferenceId, java.time.LocalDateTime deadline, Long userId);

        void closeCameraReady(Long conferenceId, String reason, Long userId);
}
