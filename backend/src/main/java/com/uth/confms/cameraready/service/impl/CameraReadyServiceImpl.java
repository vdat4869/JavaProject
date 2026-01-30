package com.uth.confms.cameraready.service.impl;

import com.uth.confms.cameraready.dto.*;
import com.uth.confms.cameraready.entity.*;
import com.uth.confms.cameraready.repository.CameraReadySubmissionRepository;
import com.uth.confms.cameraready.repository.CameraReadyVersionRepository;
import com.uth.confms.cameraready.repository.CameraReadyReviewRepository;
import com.uth.confms.cameraready.repository.CameraReadyMetadataRepository;

import com.uth.confms.cameraready.service.CameraReadyService;
import com.uth.confms.cameraready.service.PdfValidationService;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.util.FileUtil;
import com.uth.confms.common.util.PdfFontUtil;
import com.uth.confms.conference.entity.Deadline;
import com.uth.confms.conference.entity.Deadline.DeadlineType;
import com.uth.confms.conference.entity.Track;
import com.uth.confms.conference.repository.DeadlineRepository;
import com.uth.confms.conference.repository.TrackRepository;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.decision.service.NotificationService;
import com.uth.confms.storage.service.StorageService;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.entity.SubmissionAuthor;
import com.uth.confms.submission.repository.SubmissionRepository;
import com.uth.confms.submission.repository.SubmissionAuthorRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.core.io.InputStreamResource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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
import java.util.Arrays;

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
    private final SubmissionRepository submissionRepositoryMain;
    private final StorageService storageService;
    private final TrackRepository trackRepository;
    private final DeadlineRepository deadlineRepository;
    private final ConferenceRepository conferenceRepository;
    private final NotificationService notificationService;
    private final SubmissionAuthorRepository submissionAuthorRepository;

    @Override
    @Transactional
    public SubmissionDTO getSubmissionByPaperId(Long conferenceId, Long paperId) {
        log.debug("Lấy thông tin submission cho paper: {}", paperId);

        return submissionRepository.findByPaperId(paperId)
                .map(this::mapToSubmissionDTO)
                .orElseGet(() -> {
                    // Lazy init: Kiểm tra nếu paper exists và accepted thì tạo mới
                    // Nếu bài báo đã được chấp nhận nhưng chưa có bản ghi CameraReadySubmission,
                    // hệ thống sẽ tự động tạo mới (lazy initialization) để tác giả có thể bắt đầu
                    // nộp.
                    Submission originalSubmission = submissionRepositoryMain.findById(paperId)
                            .orElseThrow(() -> new NotFoundException("Paper not found"));

                    if (originalSubmission.getStatus() == Submission.SubmissionStatus.ACCEPTED) {
                        log.info("Lazy-init CameraReadySubmission cho paper {}", paperId);

                        Long trackId = originalSubmission.getTrackId();
                        if (trackId == null) {
                            // Fallback: Lấy track đầu tiên của conference
                            Track defaultTrack = trackRepository.findByConferenceId(conferenceId).stream()
                                    .findFirst()
                                    .orElseThrow(() -> new BusinessException("Conference does not have any tracks"));
                            trackId = defaultTrack.getId();
                            log.warn("Submission {} missing trackId, using default track {}", paperId, trackId);
                        }

                        CameraReadySubmission newSubmission = CameraReadySubmission.builder()
                                .paperId(originalSubmission.getId())
                                .conferenceId(conferenceId)
                                .trackId(trackId)
                                .authorId(originalSubmission.getAuthorId())
                                .status(CameraReadyStatus.OPEN)
                                .copyrightConfirmed(false)
                                .build();
                        return mapToSubmissionDTO(submissionRepository.save(newSubmission));
                    }

                    throw new NotFoundException("Submission not found or not accepted");
                });
    }

    @Override
    @Transactional
    public VersionDTO uploadVersion(Long conferenceId, Long paperId, MultipartFile file, Long uploaderId) {
        log.info("Upload phiên bản mới cho paper {} bởi user {}", paperId, uploaderId);

        CameraReadySubmission submission = submissionRepository.findByPaperId(paperId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));

        if (!submission.canUpload()) {
            throw new BusinessException("Không thể tải lên ở trạng thái hiện tại: " + submission.getStatus());
        }

        if (!submission.getAuthorId().equals(uploaderId)) {
            throw new BusinessException("Chỉ tác giả chính của bài báo mới có quyền nộp Camera-Ready");
        }

        // Check Deadline
        checkCameraReadyDeadline(conferenceId);

        // Validate PDF
        ValidationResultDTO validationResult = pdfValidationService.validate(file);

        // Tạo version mới
        int versionNumber = submission.getNextVersionNumber();

        // Store file using StorageService - Lưu file vật lý
        String storedPath = storageService.storeCameraReadyPdf(conferenceId, paperId, file);

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

        submission.addVersion(version);
        submission.setStatus(CameraReadyStatus.SUBMITTED); // Chuyển sang SUBMITTED để Chair có thể duyệt
        submissionRepository.save(submission);

        log.info("Đã upload version {} cho paper {}", versionNumber, paperId);
        return mapToVersionDTO(version);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VersionDTO> listVersions(Long conferenceId, Long paperId) {
        CameraReadySubmission submission = submissionRepository.findByPaperId(paperId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));

        return versionRepository.findBySubmissionIdOrderByVersionNumberDesc(submission.getId())
                .stream()
                .map(this::mapToVersionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadVersion(Long conferenceId, Long paperId, UUID versionId) {
        CameraReadyVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new NotFoundException("Version not found"));

        // Validate version belongs to the submission
        if (!version.getSubmission().getPaperId().equals(paperId)) {
            throw new BusinessException("Version does not belong to this paper");
        }

        // Get file stream from StorageService
        try {
            InputStream inputStream = storageService.getFileStream(version.getStoredPath());
            return new InputStreamResource(inputStream);
        } catch (Exception e) {
            log.error("Error loading file from storage: {}", version.getStoredPath(), e);
            throw new NotFoundException("File not found in storage: " + version.getStoredPath());
        }
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
    public SubmissionDTO confirmCopyright(Long conferenceId, Long paperId,
            CopyrightConfirmRequestDTO request, Long userId) {
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
    public Page<SubmissionListDTO> listSubmissions(Long conferenceId, Long trackId,
            CameraReadyStatus status, Boolean copyrightConfirmed,
            Pageable pageable) {
        log.debug("Lấy danh sách submissions cho conference {}", conferenceId);

        // Tìm kiếm với các bộ lọc
        Page<CameraReadySubmission> page = submissionRepository.findWithFilters(conferenceId, trackId, status,
                copyrightConfirmed, pageable);

        if (page.isEmpty()) {
            return page.map(s -> null); // Should not happen with empty page
        }

        List<CameraReadySubmission> content = page.getContent();
        List<Long> paperIds = content.stream().map(CameraReadySubmission::getPaperId).collect(Collectors.toList());
        Set<Long> trackIds = content.stream().map(CameraReadySubmission::getTrackId).collect(Collectors.toSet());

        // Batch fetch all related data (Performance Optimization: N+1 problem)
        // Lấy thông tin bài báo gốc, track và tác giả để map vào DTO
        Map<Long, Submission> submissionMap = submissionRepositoryMain.findAllById(paperIds).stream()
                .collect(Collectors.toMap(Submission::getId, s -> s));

        Map<Long, String> trackNameMap = trackRepository.findAllById(trackIds).stream()
                .collect(Collectors.toMap(Track::getId, Track::getName));

        Map<Long, List<SubmissionAuthor>> authorsMap = submissionAuthorRepository.findBySubmissionIdIn(paperIds)
                .stream()
                .collect(Collectors.groupingBy(a -> a.getSubmission().getId()));

        return page.map(submission -> {
            Submission original = submissionMap.get(submission.getPaperId());
            String trackName = trackNameMap.getOrDefault(submission.getTrackId(), "Unknown");
            List<SubmissionAuthor> authors = authorsMap.getOrDefault(submission.getPaperId(), List.of());

            return mapToSubmissionListDTO(submission, original, trackName, authors);
        });
    }

    @Override
    @Transactional
    public ReviewResponseDTO reviewSubmission(Long conferenceId, UUID submissionId,
            ReviewRequestDTO request, Long reviewerId) {
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
        // Nếu APPROVED -> APPROVED
        // Nếu REJECTED -> NEED_FIX (yêu cầu sửa lại)
        CameraReadyStatus newStatus = request.getDecision() == ReviewDecision.APPROVED
                ? CameraReadyStatus.APPROVED
                : CameraReadyStatus.NEED_FIX;

        submission.transitionTo(newStatus);
        submissionRepository.save(submission);

        log.info("Đã duyệt submission {} với kết quả {}", submissionId, newStatus);

        // Gửi thông báo cho tác giả
        try {
            notificationService.sendCameraReadyReviewNotification(submission, request.getDecision(), request.getNote());
        } catch (Exception e) {
            log.warn("Không thể gửi thông báo camera-ready review: {}", e.getMessage());
        }

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
    public SubmissionDTO setCurrentVersion(Long conferenceId, UUID submissionId, UUID versionId, Long userId) {
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
    public MetadataDTO updateMetadata(UUID submissionId, MetadataUpdateRequestDTO request, Long userId) {
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
    public StatisticsDTO getStatistics(Long conferenceId) {
        log.debug("Lấy thống kê cho conference {}", conferenceId);

        Map<String, Long> byStatus = new HashMap<>();
        for (CameraReadyStatus status : CameraReadyStatus.values()) {
            byStatus.put(status.name(), 0L);
        }

        List<Object[]> counts = submissionRepository.countByConferenceIdGroupByStatus(conferenceId);
        for (Object[] row : counts) {
            CameraReadyStatus status = (CameraReadyStatus) row[0];
            Long count = (Long) row[1];
            byStatus.put(status.name(), count);
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
    public ProceedingsExportDTO exportProceedingsJson(Long conferenceId, Long trackId, CameraReadyStatus status) {
        log.info("Xuất kỷ yếu JSON cho conference {}", conferenceId);

        List<CameraReadySubmission> submissions = submissionRepository
                .findByConferenceIdAndStatus(conferenceId, status);

        if (trackId != null) {
            submissions = submissions.stream()
                    .filter(s -> trackId.equals(s.getTrackId()))
                    .collect(Collectors.toList());
        }

        if (submissions.isEmpty()) {
            return ProceedingsExportDTO.builder()
                    .conferenceId(conferenceId)
                    .exportedAt(LocalDateTime.now())
                    .totalPapers(0)
                    .papers(Collections.emptyList())
                    .build();
        }

        List<Long> paperIds = submissions.stream().map(CameraReadySubmission::getPaperId).collect(Collectors.toList());
        List<UUID> submissionIds = submissions.stream().map(CameraReadySubmission::getId).collect(Collectors.toList());
        Set<Long> trackIds = submissions.stream().map(CameraReadySubmission::getTrackId).collect(Collectors.toSet());

        // Batch fetch all related data
        Map<Long, Submission> submissionMap = submissionRepositoryMain.findAllById(paperIds).stream()
                .collect(Collectors.toMap(Submission::getId, s -> s));

        Map<Long, String> trackNameMap = trackRepository.findAllById(trackIds).stream()
                .collect(Collectors.toMap(Track::getId, Track::getName));

        Map<Long, List<SubmissionAuthor>> authorsMap = submissionAuthorRepository.findBySubmissionIdIn(paperIds)
                .stream()
                .collect(Collectors.groupingBy(a -> a.getSubmission().getId()));

        Map<UUID, CameraReadyMetadata> metadataMap = metadataRepository.findBySubmissionIdIn(submissionIds).stream()
                .collect(Collectors.toMap(m -> m.getSubmission().getId(), m -> m));

        com.uth.confms.conference.entity.Conference conference = conferenceRepository.findById(conferenceId)
                .orElse(null);

        return ProceedingsExportDTO.builder()
                .conferenceId(conferenceId)
                .conferenceName(conference != null ? conference.getName() : "Unknown")
                .exportedAt(LocalDateTime.now())
                .totalPapers(submissions.size())
                .papers(submissions.stream()
                        .map(s -> {
                            Submission original = submissionMap.get(s.getPaperId());
                            CameraReadyMetadata metadata = metadataMap.get(s.getId());
                            String tName = trackNameMap.getOrDefault(s.getTrackId(), "Unknown");
                            List<SubmissionAuthor> authors = authorsMap.getOrDefault(s.getPaperId(), List.of());
                            return mapToPaperExportDTO(s, original, metadata, tName, authors);
                        })
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportProceedingsCsv(Long conferenceId, Long trackId, CameraReadyStatus status) {
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
                    paper.getPresentation() != null ? paper.getPresentation().getType() : ""));
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional
    public int openCameraReady(Long conferenceId, LocalDateTime deadline, Long userId) {
        log.info("Mở camera-ready cho conference {} bởi user {}", conferenceId, userId);

        // Lấy danh sách accepted submissions từ conference
        List<Submission> acceptedSubmissions = submissionRepositoryMain.findByConferenceId(conferenceId)
                .stream()
                .filter(s -> s.getStatus() == Submission.SubmissionStatus.ACCEPTED
                        && !Boolean.TRUE.equals(s.getWithdrawn()))
                .collect(Collectors.toList());

        if (acceptedSubmissions.isEmpty()) {
            log.warn("Không có accepted submissions nào cho conference {}", conferenceId);
            return 0;
        }

        int createdCount = 0;
        for (Submission submission : acceptedSubmissions) {
            // Check if CameraReadySubmission already exists
            if (submissionRepository.existsByPaperId(submission.getId())) {
                log.debug("Camera-ready submission đã tồn tại cho paper (submission {})", submission.getId());
                continue;
            }

            // Create CameraReadySubmission
            CameraReadySubmission cameraReadySubmission = CameraReadySubmission.builder()
                    .paperId(submission.getId())
                    .conferenceId(conferenceId)
                    .trackId(submission.getTrackId())
                    .authorId(submission.getAuthorId())
                    .status(CameraReadyStatus.OPEN)
                    .copyrightConfirmed(false)
                    .build();

            submissionRepository.save(cameraReadySubmission);
            createdCount++;
            log.debug("Đã tạo camera-ready submission cho paper (submission {})", submission.getId());
        }

        log.info("Đã mở camera-ready cho {} papers trong conference {}", createdCount, conferenceId);

        if (deadline != null) {
            updateCameraReadyDeadline(conferenceId, deadline);
        }

        // Send notifications
        if (createdCount > 0) {
            // Because this is a bulk operation, we spawn a background task or just iterate
            // For simplicity and since we have the list of submissions effectively (we know
            // they are the accepted ones),
            // we can trigger notifications.
            // But we need the submission object. Using acceptedSubmissions list.
            for (Submission submission : acceptedSubmissions) {
                try {
                    notificationService.sendCameraReadyOpenNotification(submission, deadline);
                } catch (Exception e) {
                    log.error("Failed to send camera-ready open notification for submission {}", submission.getId(), e);
                }
            }
        }

        return createdCount;
    }

    private void updateCameraReadyDeadline(Long conferenceId, LocalDateTime deadlineDate) {
        com.uth.confms.conference.entity.Conference conference = conferenceRepository.findById(conferenceId)
                .orElseThrow(() -> new NotFoundException("Conference not found"));

        Deadline deadline = deadlineRepository.findByConferenceId(conferenceId).stream()
                .filter(d -> d.getType() == DeadlineType.CAMERA_READY)
                .findFirst()
                .orElse(null);

        if (deadline == null) {
            deadline = Deadline.builder()
                    .conference(conference)
                    .type(DeadlineType.CAMERA_READY)
                    .dueDate(deadlineDate)
                    .description("Camera Ready Submission Deadline")
                    .hardDeadline(true)
                    .build();
        } else {
            deadline.setDueDate(deadlineDate);
        }
        deadlineRepository.save(deadline);
    }

    private void checkCameraReadyDeadline(Long conferenceId) {
        Deadline deadline = deadlineRepository.findByConferenceId(conferenceId).stream()
                .filter(d -> d.getType() == DeadlineType.CAMERA_READY)
                .findFirst()
                .orElse(null);

        if (deadline != null && LocalDateTime.now().isAfter(deadline.getDueDate())) {
            throw new BusinessException("Đã quá hạn nộp Camera-Ready (" + deadline.getDueDate() + ")");
        }
    }

    @Override
    @Transactional
    public void closeCameraReady(Long conferenceId, String reason, Long userId) {
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
        LocalDateTime formattedDeadline = null;
        Deadline deadline = deadlineRepository.findByConferenceId(submission.getConferenceId()).stream()
                .filter(d -> d.getType() == DeadlineType.CAMERA_READY)
                .findFirst()
                .orElse(null);
        if (deadline != null) {
            formattedDeadline = deadline.getDueDate();
        }

        return SubmissionDTO.builder()
                .id(submission.getId())
                .paperId(submission.getPaperId())
                .conferenceId(submission.getConferenceId())
                .trackId(submission.getTrackId())
                .status(submission.getStatus())
                .currentVersion(submission.getCurrentVersion() != null
                        ? mapToVersionDTO(submission.getCurrentVersion())
                        : null)
                .copyrightConfirmed(submission.getCopyrightConfirmed())
                .copyrightConfirmedAt(submission.getCopyrightConfirmedAt())
                .canUpload(submission.canUpload())
                .canConfirmCopyright(!Boolean.TRUE.equals(submission.getCopyrightConfirmed()))
                .createdAt(submission.getCreatedAt())
                .updatedAt(submission.getUpdatedAt())
                .deadline(formattedDeadline)
                .build();
    }

    private SubmissionListDTO mapToSubmissionListDTO(CameraReadySubmission submission,
            Submission original, String trackName, List<SubmissionAuthor> authors) {

        String paperTitle = original != null ? original.getTitle() : "Unknown";
        VersionDTO.UserDTO authorDTO = null;

        // Get Corresponding Author
        SubmissionAuthor correspondingAuthor = authors.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCorresponding()))
                .findFirst()
                .orElse(authors.stream().findFirst().orElse(null));

        if (correspondingAuthor != null) {
            authorDTO = VersionDTO.UserDTO.builder()
                    .id(correspondingAuthor.getUserId())
                    .fullName(correspondingAuthor.getFirstName() + " " + correspondingAuthor.getLastName())
                    .email(correspondingAuthor.getEmail())
                    .build();
        }

        return SubmissionListDTO.builder()
                .id(submission.getId())
                .paperId(submission.getPaperId())
                .paperTitle(paperTitle)
                .trackId(submission.getTrackId())
                .trackName(trackName)
                .status(submission.getStatus())
                .currentVersionNumber(submission.getCurrentVersion() != null
                        ? submission.getCurrentVersion().getVersionNumber()
                        : null)
                .copyrightConfirmed(submission.getCopyrightConfirmed())
                .correspondingAuthor(authorDTO)
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

    private ProceedingsExportDTO.PaperExportDTO mapToPaperExportDTO(CameraReadySubmission submission,
            Submission original, CameraReadyMetadata metadata, String trackName, List<SubmissionAuthor> authors) {

        List<ProceedingsExportDTO.AuthorExportDTO> authorDTOs = authors.stream()
                .map(a -> ProceedingsExportDTO.AuthorExportDTO.builder()
                        .name(a.getFirstName() + " " + a.getLastName())
                        .email(a.getEmail())
                        .affiliation(a.getAffiliation())
                        .isCorresponding(a.getIsCorresponding())
                        .build())
                .collect(Collectors.toList());

        return ProceedingsExportDTO.PaperExportDTO.builder()
                .paperId(submission.getPaperId())
                .title(original != null ? original.getTitle() : "Unknown")
                .abstractText(original != null ? original.getAbstractText() : null)
                .keywords(original != null && original.getKeywords() != null
                        ? Arrays.asList(original.getKeywords().split(","))
                        : null)
                .authors(authorDTOs)
                .doi(metadata != null ? metadata.getDoi() : null)
                .startPage(metadata != null ? metadata.getStartPage() : null)
                .endPage(metadata != null ? metadata.getEndPage() : null)
                .pdfPath(submission.getCurrentVersion() != null
                        ? submission.getCurrentVersion().getStoredPath()
                        : null)
                .track(ProceedingsExportDTO.TrackExportDTO.builder()
                        .id(submission.getTrackId())
                        .name(trackName)
                        .build())
                .presentation(metadata != null && metadata.getPresentationType() != null
                        ? ProceedingsExportDTO.PresentationExportDTO.builder()
                                .type(metadata.getPresentationType().toString())
                                .durationMinutes(metadata.getPresentationDurationMinutes())
                                .build()
                        : null)
                .build();
    }

    private String calculateChecksum(MultipartFile file) {
        try {
            return FileUtil.calculateChecksum(file);
        } catch (Exception e) {
            log.error("Error calculating checksum for file: {}", file.getOriginalFilename(), e);
            // Fallback: return empty string if checksum calculation fails
            return "";
        }
    }

    private Map<String, Object> convertValidationToMap(ValidationResultDTO result) {
        Map<String, Object> map = new HashMap<>();
        map.put("passed", result.isPassed());
        map.put("pageCount", result.getPageCount());
        map.put("pageSize", result.getPageSize());
        return map;
    }

    private String escapeCSV(String value) {
        if (value == null)
            return "";
        return value.replace("\"", "\"\"");
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportProceedingsZip(Long conferenceId, Long trackId, CameraReadyStatus status) {
        log.info("Xuất kỷ yếu ZIP cho conference {}", conferenceId);

        ProceedingsExportDTO export = exportProceedingsJson(conferenceId, trackId, status);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos)) {

            // Add metadata JSON file
            String jsonContent = convertToJson(export);
            ZipEntry jsonEntry = new ZipEntry("proceedings_metadata.json");
            zos.putNextEntry(jsonEntry);
            zos.write(jsonContent.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // Add CSV file
            byte[] csvContent = exportProceedingsCsv(conferenceId, trackId, status);
            ZipEntry csvEntry = new ZipEntry("proceedings.csv");
            zos.putNextEntry(csvEntry);
            zos.write(csvContent);
            zos.closeEntry();

            // Add PDF files
            int paperIndex = 1;
            for (ProceedingsExportDTO.PaperExportDTO paper : export.getPapers()) {
                if (paper.getPdfPath() != null) {
                    try {
                        // Get PDF file from storage
                        InputStream pdfStream = storageService.getFileStream(paper.getPdfPath());

                        // Read PDF into byte array
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        byte[] data = new byte[8192];
                        int nRead;
                        while ((nRead = pdfStream.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }
                        byte[] pdfBytes = buffer.toByteArray();
                        pdfStream.close();

                        // Add to ZIP with organized filename
                        String filename = String.format("papers/%03d_%s.pdf",
                                paperIndex++,
                                sanitizeFilename(paper.getTitle()));
                        ZipEntry pdfEntry = new ZipEntry(filename);
                        zos.putNextEntry(pdfEntry);
                        zos.write(pdfBytes);
                        zos.closeEntry();
                    } catch (Exception e) {
                        log.warn("Không thể thêm PDF cho paper {}: {}", paper.getPaperId(), e.getMessage());
                        // Continue with other papers
                    }
                }
            }

            zos.finish();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error creating ZIP export for conference {}", conferenceId, e);
            throw new BusinessException("Không thể tạo file ZIP: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportProceedingsPdf(Long conferenceId, Long trackId, CameraReadyStatus status) {
        log.info("Xuất kỷ yếu PDF cho conference {}", conferenceId);

        ProceedingsExportDTO export = exportProceedingsJson(conferenceId, trackId, status);

        List<PDDocument> sourceDocuments = new ArrayList<>(); // Giữ các document nguồn mở

        try (PDDocument mergedDocument = new PDDocument()) {
            int currentPageNumber = 1;

            // Add title page
            addTitlePage(mergedDocument, export);
            currentPageNumber++;

            // Add table of contents
            int tocPages = addTableOfContents(mergedDocument, export, currentPageNumber);
            currentPageNumber += tocPages;

            // Merge all paper PDFs with automatic page numbering
            for (ProceedingsExportDTO.PaperExportDTO paper : export.getPapers()) {
                int startPage = currentPageNumber;

                if (paper.getPdfPath() != null) {
                    try {
                        // Get PDF file from storage
                        InputStream pdfStream = storageService.getFileStream(paper.getPdfPath());

                        // Read PDF into byte array
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        byte[] data = new byte[8192];
                        int nRead;
                        while ((nRead = pdfStream.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }
                        byte[] pdfBytes = buffer.toByteArray();
                        pdfStream.close();

                        PDDocument paperDoc = Loader.loadPDF(pdfBytes);
                        sourceDocuments.add(paperDoc); // Thêm vào list để đóng sau

                        // Import pages từ paper vào merged document (tạo bản copy thay vì reference)
                        for (int i = 0; i < paperDoc.getNumberOfPages(); i++) {
                            mergedDocument.importPage(paperDoc.getPage(i));
                            currentPageNumber++;
                        }

                        int endPage = currentPageNumber - 1;

                        // Update metadata with automatic page numbers if not set
                        if (paper.getStartPage() == null || paper.getEndPage() == null) {
                            updatePaperPageNumbers(paper.getPaperId(), startPage, endPage);
                        }

                        // KHÔNG đóng paperDoc ở đây - sẽ đóng sau khi save merged document
                    } catch (Exception e) {
                        log.warn("Không thể thêm PDF cho paper {}: {}", paper.getPaperId(), e.getMessage());
                        // Add placeholder page
                        addPlaceholderPage(mergedDocument, paper.getTitle());
                        currentPageNumber++;
                    }
                } else {
                    // Add placeholder if no PDF
                    addPlaceholderPage(mergedDocument, paper.getTitle());
                    currentPageNumber++;
                }
            }

            // Save merged PDF to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mergedDocument.save(baos);

            // Đóng tất cả các source documents SAU khi đã save
            for (PDDocument doc : sourceDocuments) {
                try {
                    doc.close();
                } catch (Exception e) {
                    log.debug("Error closing source document: {}", e.getMessage());
                }
            }

            return baos.toByteArray();
        } catch (Exception e) {
            // Đảm bảo đóng các source documents nếu có lỗi
            for (PDDocument doc : sourceDocuments) {
                try {
                    doc.close();
                } catch (Exception ex) {
                    log.debug("Error closing source document: {}", ex.getMessage());
                }
            }
            log.error("Error creating PDF export for conference {}", conferenceId, e);
            throw new BusinessException("Không thể tạo file PDF: " + e.getMessage());
        }
    }

    private void addTitlePage(PDDocument document, ProceedingsExportDTO export) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // Load Unicode-compatible fonts for Vietnamese support
        PDFont boldFont = PdfFontUtil.loadBoldFont(document);
        PDFont regularFont = PdfFontUtil.loadRegularFont(document);
        boolean hasUnicodeFont = PdfFontUtil.hasCustomFonts();

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(boldFont, 24);
            contentStream.newLineAtOffset(50, 750);
            String conferenceName = export.getConferenceName() != null ? export.getConferenceName()
                    : "Conference Proceedings";
            contentStream.showText(PdfFontUtil.prepareText(conferenceName, hasUnicodeFont));
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(regularFont, 12);
            contentStream.newLineAtOffset(50, 700);
            contentStream.showText("Total Papers: " + export.getTotalPapers());
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(regularFont, 12);
            contentStream.newLineAtOffset(50, 680);
            contentStream.showText("Exported: " + export.getExportedAt().toString());
            contentStream.endText();
        }
    }

    private int addTableOfContents(PDDocument document, ProceedingsExportDTO export, int startPage) throws Exception {
        int currentPage = startPage;
        int itemsPerPage = 40;
        int totalItems = export.getPapers().size();
        int totalPages = (totalItems + itemsPerPage - 1) / itemsPerPage;

        // Load Unicode-compatible fonts for Vietnamese support
        PDFont boldFont = PdfFontUtil.loadBoldFont(document);
        PDFont regularFont = PdfFontUtil.loadRegularFont(document);
        boolean hasUnicodeFont = PdfFontUtil.hasCustomFonts();

        for (int pageNum = 0; pageNum < totalPages; pageNum++) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Title
                contentStream.beginText();
                contentStream.setFont(boldFont, 18);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Table of Contents");
                contentStream.endText();

                // Table entries
                int startIdx = pageNum * itemsPerPage;
                int endIdx = Math.min(startIdx + itemsPerPage, totalItems);
                int yPos = 720;

                for (int i = startIdx; i < endIdx; i++) {
                    ProceedingsExportDTO.PaperExportDTO paper = export.getPapers().get(i);
                    int pageNumForPaper = paper.getStartPage() != null ? paper.getStartPage()
                            : (currentPage + i - startIdx + 1);

                    contentStream.beginText();
                    contentStream.setFont(regularFont, 10);
                    contentStream.newLineAtOffset(50, yPos);
                    String title = paper.getTitle() != null ? paper.getTitle() : "Untitled";
                    if (title.length() > 60) {
                        title = title.substring(0, 57) + "...";
                    }
                    contentStream
                            .showText(String.format("%d. %s", i + 1, PdfFontUtil.prepareText(title, hasUnicodeFont)));
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(regularFont, 10);
                    contentStream.newLineAtOffset(500, yPos);
                    contentStream.showText(String.valueOf(pageNumForPaper));
                    contentStream.endText();

                    yPos -= 15;
                }
            }
            currentPage++;
        }

        return totalPages;
    }

    private void addPlaceholderPage(PDDocument document, String title) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // Load Unicode-compatible fonts for Vietnamese support
        PDFont boldFont = PdfFontUtil.loadBoldFont(document);
        PDFont regularFont = PdfFontUtil.loadRegularFont(document);
        boolean hasUnicodeFont = PdfFontUtil.hasCustomFonts();

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(boldFont, 16);
            contentStream.newLineAtOffset(50, 750);
            String displayTitle = title != null ? title : "Paper Not Available";
            contentStream.showText(PdfFontUtil.prepareText(displayTitle, hasUnicodeFont));
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(regularFont, 12);
            contentStream.newLineAtOffset(50, 700);
            contentStream.showText("PDF file is not available for this paper.");
            contentStream.endText();
        }
    }

    private String convertToJson(ProceedingsExportDTO export) {
        // Simple JSON conversion (can be improved with Jackson/ObjectMapper)
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"conferenceId\": \"").append(export.getConferenceId()).append("\",\n");
        json.append("  \"conferenceName\": \"")
                .append(export.getConferenceName() != null ? export.getConferenceName() : "").append("\",\n");
        json.append("  \"exportedAt\": \"").append(export.getExportedAt()).append("\",\n");
        json.append("  \"totalPapers\": ").append(export.getTotalPapers()).append(",\n");
        json.append("  \"papers\": [\n");
        for (int i = 0; i < export.getPapers().size(); i++) {
            ProceedingsExportDTO.PaperExportDTO paper = export.getPapers().get(i);
            json.append("    {\n");
            json.append("      \"paperId\": \"").append(paper.getPaperId()).append("\",\n");
            json.append("      \"title\": \"").append(escapeJson(paper.getTitle())).append("\",\n");
            json.append("      \"doi\": \"").append(paper.getDoi() != null ? paper.getDoi() : "").append("\",\n");
            json.append("      \"startPage\": ").append(paper.getStartPage() != null ? paper.getStartPage() : "null")
                    .append(",\n");
            json.append("      \"endPage\": ").append(paper.getEndPage() != null ? paper.getEndPage() : "null")
                    .append("\n");
            json.append("    }");
            if (i < export.getPapers().size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ]\n");
        json.append("}\n");
        return json.toString();
    }

    private String escapeJson(String value) {
        if (value == null)
            return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String sanitizeFilename(String filename) {
        if (filename == null)
            return "untitled";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_")
                .substring(0, Math.min(filename.length(), 50));
    }

    /**
     * Update page numbers for a paper in metadata
     */
    private void updatePaperPageNumbers(Long paperId, int startPage, int endPage) {
        try {
            CameraReadySubmission submission = submissionRepository.findByPaperId(paperId)
                    .orElse(null);
            if (submission != null && submission.getMetadata() != null) {
                CameraReadyMetadata metadata = submission.getMetadata();
                metadata.setStartPage(startPage);
                metadata.setEndPage(endPage);
                metadataRepository.save(metadata);
                log.debug("Updated page numbers for paper {}: {}-{}", paperId, startPage, endPage);
            }
        } catch (Exception e) {
            log.warn("Could not update page numbers for paper {}: {}", paperId, e.getMessage());
        }
    }

}
