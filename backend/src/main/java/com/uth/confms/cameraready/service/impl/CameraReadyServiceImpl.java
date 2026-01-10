package com.uth.confms.cameraready.service.impl;

import com.uth.confms.cameraready.dto.*;
import com.uth.confms.cameraready.entity.*;
import com.uth.confms.cameraready.repository.*;
import com.uth.confms.cameraready.service.CameraReadyService;
import com.uth.confms.cameraready.service.PdfValidationService;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation của CameraReadyService.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CameraReadyServiceImpl implements CameraReadyService {

    private final CameraReadySubmissionRepository submissionRepository;
    private final CameraReadyVersionRepository versionRepository;
    private final CameraReadyReviewRepository reviewRepository;
    private final CameraReadyMetadataRepository metadataRepository;
    private final PdfValidationService pdfValidationService;
    // TODO: Inject FileStorageService từ module storage của team
    // private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public SubmissionDTO getSubmissionByPaperId(UUID conferenceId, UUID paperId) {
        log.debug("Lấy thông tin submission cho paper: {}", paperId);
        
        CameraReadySubmission submission = submissionRepository.findByPaperId(paperId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
        
        return mapToSubmissionDTO(submission);
    }

    @Override
    @Transactional
    public VersionDTO uploadVersion(UUID conferenceId, UUID paperId, MultipartFile file, UUID uploaderId) {
        log.info("Upload phiên bản mới cho paper {} bởi user {}", paperId, uploaderId);
        
        CameraReadySubmission submission = submissionRepository.findByPaperId(paperId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));

        if (!submission.canUpload()) {
            throw new BusinessException("Không thể tải lên ở trạng thái hiện tại: " + submission.getStatus());
        }

        // Validate PDF
        ValidationResultDTO validationResult = pdfValidationService.validate(file);

        // Tạo version mới
        int versionNumber = submission.getNextVersionNumber();
        String storedPath = generateStoredPath(conferenceId, paperId, versionNumber);

        CameraReadyVersion version = CameraReadyVersion.builder()
                .submission(submission)
                .versionNumber(versionNumber)
                .originalFilename(file.getOriginalFilename())
                .storedPath(storedPath)
                .fileSizeBytes(file.getSize())
                .checksumSha256(calculateChecksum(file))
                .pageCount(validationResult.getPageCount())
                .pageSize(validationResult.getPageSize())
                .validationResult(convertValidationToMap(validationResult))
                .validationPassed(validationResult.isPassed())
                .uploadedBy(uploaderId)
                .uploadedAt(LocalDateTime.now())
                .build();

        // TODO: Lưu file sử dụng FileStorageService
        // fileStorageService.store(file, storedPath);

        submission.addVersion(version);
        submissionRepository.save(submission);

        log.info("Đã upload version {} cho paper {}", versionNumber, paperId);
        return mapToVersionDTO(version);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VersionDTO> listVersions(UUID conferenceId, UUID paperId) {
        CameraReadySubmission submission = submissionRepository.findByPaperId(paperId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));

        return versionRepository.findBySubmissionIdOrderByVersionNumberDesc(submission.getId())
                .stream()
                .map(this::mapToVersionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadVersion(UUID conferenceId, UUID paperId, UUID versionId) {
        versionRepository.findById(versionId)
                .orElseThrow(() -> new NotFoundException("Version not found"));

        // TODO: Sử dụng FileStorageService
        // return fileStorageService.loadAsResource(version.getStoredPath());
        throw new UnsupportedOperationException("Cần tích hợp với FileStorageService");
    }

    @Override
    @Transactional(readOnly = true)
    public String getVersionFilename(UUID versionId) {
        return versionRepository.findById(versionId)
                .map(CameraReadyVersion::getOriginalFilename)
                .orElseThrow(() -> new NotFoundException("Version not found"));
    }

    @Override
    @Transactional
    public SubmissionDTO confirmCopyright(UUID conferenceId, UUID paperId,
                                           CopyrightConfirmRequestDTO request, UUID userId) {
        log.info("Xác nhận bản quyền cho paper {} bởi user {}", paperId, userId);
        
        CameraReadySubmission submission = submissionRepository.findByPaperId(paperId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));

        if (!Boolean.TRUE.equals(request.getConfirmed())) {
            throw new BusinessException("Phải xác nhận bản quyền");
        }

        submission.confirmCopyright(userId);
        submissionRepository.save(submission);

        log.info("Đã xác nhận bản quyền cho paper {}", paperId);
        return mapToSubmissionDTO(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubmissionListDTO> listSubmissions(UUID conferenceId, UUID trackId,
                                                    CameraReadyStatus status, Boolean copyrightConfirmed,
                                                    Pageable pageable) {
        log.debug("Lấy danh sách submissions cho conference {}", conferenceId);
        
        return submissionRepository.findWithFilters(conferenceId, trackId, status, copyrightConfirmed, pageable)
                .map(this::mapToSubmissionListDTO);
    }

    @Override
    @Transactional
    public ReviewResponseDTO reviewSubmission(UUID conferenceId, UUID submissionId,
                                               ReviewRequestDTO request, UUID reviewerId) {
        log.info("Duyệt submission {} với quyết định {} bởi user {}", 
                submissionId, request.getDecision(), reviewerId);
        
        CameraReadySubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));

        if (!submission.canReview()) {
            throw new BusinessException("Không thể duyệt ở trạng thái hiện tại");
        }

        CameraReadyVersion version = request.getVersionId() != null
                ? versionRepository.findById(request.getVersionId())
                        .orElseThrow(() -> new NotFoundException("Version not found"))
                : submission.getCurrentVersion();

        if (version == null) {
            throw new BusinessException("Chưa có phiên bản nào được tải lên");
        }

        // Tạo review record
        CameraReadyReview review = CameraReadyReview.builder()
                .submission(submission)
                .version(version)
                .decision(request.getDecision())
                .note(request.getNote())
                .reviewedBy(reviewerId)
                .reviewedAt(LocalDateTime.now())
                .build();

        reviewRepository.save(review);

        // Cập nhật trạng thái
        CameraReadyStatus newStatus = request.getDecision() == ReviewDecision.APPROVED
                ? CameraReadyStatus.APPROVED
                : CameraReadyStatus.NEED_FIX;
        
        submission.transitionTo(newStatus);
        submissionRepository.save(submission);

        log.info("Đã duyệt submission {} với kết quả {}", submissionId, newStatus);

        return ReviewResponseDTO.builder()
                .reviewId(review.getId())
                .submissionId(submissionId)
                .decision(request.getDecision())
                .note(request.getNote())
                .newStatus(newStatus)
                .reviewedBy(VersionDTO.UserDTO.builder().id(reviewerId).build())
                .reviewedAt(review.getReviewedAt())
                .build();
    }

    @Override
    @Transactional
    public SubmissionDTO setCurrentVersion(UUID conferenceId, UUID submissionId, UUID versionId, UUID userId) {
        log.info("Đặt current version {} cho submission {}", versionId, submissionId);
        
        CameraReadySubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));

        CameraReadyVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new NotFoundException("Version not found"));

        if (!version.getSubmission().getId().equals(submissionId)) {
            throw new BusinessException("Version không thuộc về submission này");
        }

        submission.setCurrentVersion(version);
        submissionRepository.save(submission);

        return mapToSubmissionDTO(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public MetadataDTO getMetadata(UUID submissionId) {
        return metadataRepository.findBySubmissionId(submissionId)
                .map(this::mapToMetadataDTO)
                .orElse(MetadataDTO.builder().submissionId(submissionId).build());
    }

    @Override
    @Transactional
    public MetadataDTO updateMetadata(UUID submissionId, MetadataUpdateRequestDTO request, UUID userId) {
        log.info("Cập nhật metadata cho submission {}", submissionId);
        
        CameraReadySubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));

        CameraReadyMetadata metadata = metadataRepository.findBySubmissionId(submissionId)
                .orElseGet(() -> CameraReadyMetadata.builder().submission(submission).build());

        metadata.setDoi(request.getDoi());
        metadata.setStartPage(request.getStartPage());
        metadata.setEndPage(request.getEndPage());
        metadata.setPresentationType(request.getPresentationType());
        metadata.setPresentationDurationMinutes(request.getPresentationDurationMinutes());
        metadata.setExtraMetadata(request.getExtraMetadata());

        metadataRepository.save(metadata);

        return mapToMetadataDTO(metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public StatisticsDTO getStatistics(UUID conferenceId) {
        log.debug("Lấy thống kê cho conference {}", conferenceId);
        
        Map<String, Long> byStatus = new HashMap<>();
        for (CameraReadyStatus status : CameraReadyStatus.values()) {
            byStatus.put(status.name(), submissionRepository.countByConferenceIdAndStatus(conferenceId, status));
        }

        long total = submissionRepository.countByConferenceId(conferenceId);
        long copyrightConfirmed = submissionRepository.countByConferenceIdAndCopyrightConfirmedTrue(conferenceId);

        return StatisticsDTO.builder()
                .conferenceId(conferenceId)
                .totalAcceptedPapers((int) total)
                .statistics(StatisticsDTO.StatusStatistics.builder()
                        .byStatus(byStatus)
                        .copyrightConfirmed(copyrightConfirmed)
                        .copyrightPending(total - copyrightConfirmed)
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProceedingsExportDTO exportProceedingsJson(UUID conferenceId, UUID trackId, CameraReadyStatus status) {
        log.info("Xuất kỷ yếu JSON cho conference {}", conferenceId);
        
        List<CameraReadySubmission> submissions = submissionRepository
                .findByConferenceIdAndStatus(conferenceId, status);

        if (trackId != null) {
            submissions = submissions.stream()
                    .filter(s -> trackId.equals(s.getTrackId()))
                    .collect(Collectors.toList());
        }

        return ProceedingsExportDTO.builder()
                .conferenceId(conferenceId)
                .exportedAt(LocalDateTime.now())
                .totalPapers(submissions.size())
                .papers(submissions.stream()
                        .map(this::mapToPaperExportDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportProceedingsCsv(UUID conferenceId, UUID trackId, CameraReadyStatus status) {
        log.info("Xuất kỷ yếu CSV cho conference {}", conferenceId);
        
        ProceedingsExportDTO export = exportProceedingsJson(conferenceId, trackId, status);
        
        StringBuilder csv = new StringBuilder();
        csv.append("paper_id,title,doi,start_page,end_page,presentation_type\n");
        
        for (ProceedingsExportDTO.PaperExportDTO paper : export.getPapers()) {
            csv.append(String.format("%s,\"%s\",%s,%d,%d,%s\n",
                    paper.getPaperId(),
                    escapeCSV(paper.getTitle()),
                    paper.getDoi() != null ? paper.getDoi() : "",
                    paper.getStartPage() != null ? paper.getStartPage() : 0,
                    paper.getEndPage() != null ? paper.getEndPage() : 0,
                    paper.getPresentation() != null ? paper.getPresentation().getType() : ""
            ));
        }
        
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional
    public int openCameraReady(UUID conferenceId, UUID userId) {
        log.info("Mở camera-ready cho conference {} bởi user {}", conferenceId, userId);
        // TODO: Lấy danh sách papers accepted từ module decision
        return 0;
    }

    @Override
    @Transactional
    public void closeCameraReady(UUID conferenceId, String reason, UUID userId) {
        log.info("Đóng camera-ready cho conference {}", conferenceId);
        
        List<CameraReadySubmission> submissions = submissionRepository.findByConferenceId(conferenceId);
        
        for (CameraReadySubmission submission : submissions) {
            if (submission.getStatus() != CameraReadyStatus.CLOSED) {
                submission.setStatus(CameraReadyStatus.CLOSED);
            }
        }
        
        submissionRepository.saveAll(submissions);
    }

    // ==================== Helper Methods ====================

    private SubmissionDTO mapToSubmissionDTO(CameraReadySubmission submission) {
        return SubmissionDTO.builder()
                .id(submission.getId())
                .paperId(submission.getPaperId())
                .conferenceId(submission.getConferenceId())
                .trackId(submission.getTrackId())
                .status(submission.getStatus())
                .currentVersion(submission.getCurrentVersion() != null 
                        ? mapToVersionDTO(submission.getCurrentVersion()) : null)
                .copyrightConfirmed(submission.getCopyrightConfirmed())
                .copyrightConfirmedAt(submission.getCopyrightConfirmedAt())
                .canUpload(submission.canUpload())
                .canConfirmCopyright(!Boolean.TRUE.equals(submission.getCopyrightConfirmed()))
                .createdAt(submission.getCreatedAt())
                .updatedAt(submission.getUpdatedAt())
                .build();
    }

    private SubmissionListDTO mapToSubmissionListDTO(CameraReadySubmission submission) {
        return SubmissionListDTO.builder()
                .id(submission.getId())
                .paperId(submission.getPaperId())
                .trackId(submission.getTrackId())
                .status(submission.getStatus())
                .currentVersionNumber(submission.getCurrentVersion() != null 
                        ? submission.getCurrentVersion().getVersionNumber() : null)
                .copyrightConfirmed(submission.getCopyrightConfirmed())
                .updatedAt(submission.getUpdatedAt())
                .build();
    }

    private VersionDTO mapToVersionDTO(CameraReadyVersion version) {
        return VersionDTO.builder()
                .id(version.getId())
                .submissionId(version.getSubmission().getId())
                .versionNumber(version.getVersionNumber())
                .originalFilename(version.getOriginalFilename())
                .fileSizeBytes(version.getFileSizeBytes())
                .checksumSha256(version.getChecksumSha256())
                .pageCount(version.getPageCount())
                .pageSize(version.getPageSize())
                .validationPassed(version.getValidationPassed())
                .uploadedBy(VersionDTO.UserDTO.builder().id(version.getUploadedBy()).build())
                .uploadedAt(version.getUploadedAt())
                .isCurrent(version.isCurrent())
                .build();
    }

    private MetadataDTO mapToMetadataDTO(CameraReadyMetadata metadata) {
        return MetadataDTO.builder()
                .submissionId(metadata.getSubmission().getId())
                .doi(metadata.getDoi())
                .startPage(metadata.getStartPage())
                .endPage(metadata.getEndPage())
                .presentationType(metadata.getPresentationType())
                .presentationDurationMinutes(metadata.getPresentationDurationMinutes())
                .extraMetadata(metadata.getExtraMetadata())
                .updatedAt(metadata.getUpdatedAt())
                .build();
    }

    private ProceedingsExportDTO.PaperExportDTO mapToPaperExportDTO(CameraReadySubmission submission) {
        return ProceedingsExportDTO.PaperExportDTO.builder()
                .paperId(submission.getPaperId())
                .pdfPath(submission.getCurrentVersion() != null 
                        ? submission.getCurrentVersion().getStoredPath() : null)
                .build();
    }

    private String generateStoredPath(UUID conferenceId, UUID paperId, int versionNumber) {
        return String.format("conferences/%s/papers/%s/camera-ready/v%d.pdf",
                conferenceId, paperId, versionNumber);
    }

    private String calculateChecksum(MultipartFile file) {
        // TODO: Implement SHA-256 checksum
        return UUID.randomUUID().toString();
    }

    private Map<String, Object> convertValidationToMap(ValidationResultDTO result) {
        Map<String, Object> map = new HashMap<>();
        map.put("passed", result.isPassed());
        map.put("pageCount", result.getPageCount());
        map.put("pageSize", result.getPageSize());
        return map;
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
