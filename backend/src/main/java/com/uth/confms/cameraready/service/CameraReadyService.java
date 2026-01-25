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

    SubmissionDTO getSubmissionByPaperId(UUID conferenceId, UUID paperId);

    VersionDTO uploadVersion(UUID conferenceId, UUID paperId, MultipartFile file, UUID uploaderId);

    List<VersionDTO> listVersions(UUID conferenceId, UUID paperId);

    Resource downloadVersion(UUID conferenceId, UUID paperId, UUID versionId);

    String getVersionFilename(UUID versionId);

    SubmissionDTO confirmCopyright(UUID conferenceId, UUID paperId, CopyrightConfirmRequestDTO request, UUID userId);

    // ==================== Chair Operations ====================

    Page<SubmissionListDTO> listSubmissions(UUID conferenceId, UUID trackId, 
                                             CameraReadyStatus status, Boolean copyrightConfirmed, 
                                             Pageable pageable);

    ReviewResponseDTO reviewSubmission(UUID conferenceId, UUID submissionId, 
                                        ReviewRequestDTO request, UUID reviewerId);

    SubmissionDTO setCurrentVersion(UUID conferenceId, UUID submissionId, UUID versionId, UUID userId);

    MetadataDTO getMetadata(UUID submissionId);

    MetadataDTO updateMetadata(UUID submissionId, MetadataUpdateRequestDTO request, UUID userId);

    StatisticsDTO getStatistics(UUID conferenceId);

    ProceedingsExportDTO exportProceedingsJson(UUID conferenceId, UUID trackId, CameraReadyStatus status);

    byte[] exportProceedingsCsv(UUID conferenceId, UUID trackId, CameraReadyStatus status);

    byte[] exportProceedingsZip(UUID conferenceId, UUID trackId, CameraReadyStatus status);

    byte[] exportProceedingsPdf(UUID conferenceId, UUID trackId, CameraReadyStatus status);

    // ==================== Admin Operations ====================

    int openCameraReady(UUID conferenceId, UUID userId);

    void closeCameraReady(UUID conferenceId, String reason, UUID userId);
}
