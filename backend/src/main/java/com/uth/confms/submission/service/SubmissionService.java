package com.uth.confms.submission.service;

import com.uth.confms.assignment.entity.Assignment;
import com.uth.confms.assignment.repository.AssignmentRepository;
import com.uth.confms.auth.service.UserService;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.conference.entity.Deadline;
import com.uth.confms.conference.entity.Deadline.DeadlineType;
import com.uth.confms.conference.repository.DeadlineRepository;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.conference.repository.TrackRepository;
import com.uth.confms.pc.repository.PCMemberRepository;
import com.uth.confms.pc.service.COIService;
import com.uth.confms.storage.service.StorageService;
import com.uth.confms.submission.dto.SubmissionAuthorDTO;
import com.uth.confms.submission.dto.SubmissionCreateDTO;
import com.uth.confms.submission.dto.SubmissionFileDTO;
import com.uth.confms.submission.dto.SubmissionResponseDTO;
import com.uth.confms.submission.dto.SubmissionUpdateDTO;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.entity.SubmissionAuthor;
import com.uth.confms.submission.entity.SubmissionFile;
import com.uth.confms.submission.repository.SubmissionAuthorRepository;
import com.uth.confms.submission.repository.SubmissionFileRepository;
import com.uth.confms.submission.repository.SubmissionRepository;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service quản lý submissions (bài nộp)
 *
 * <p>
 * Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 * <li>Tạo, cập nhật, xóa submission
 * <li>Upload PDF file
 * <li>Submit và withdraw submission
 * <li>Quản lý authors và metadata
 * <li>Kiểm tra deadline
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class SubmissionService {
  private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);

  private final SubmissionRepository submissionRepository;
  private final SubmissionAuthorRepository submissionAuthorRepository;
  private final SubmissionFileRepository submissionFileRepository;
  private final DeadlineRepository deadlineRepository;
  private final ConferenceRepository conferenceRepository;
  private final StorageService storageService;
  private final COIService coiService;
  private final PCMemberRepository pcMemberRepository;
  private final AssignmentRepository assignmentRepository;
  private final UserService userService;
  private final TrackRepository trackRepository;

  @Value("${app.submission.deadline.grace-period-hours:24}")
  private int gracePeriodHours;

  @Value("${app.submission.deadline.enable-logging:true}")
  private boolean enableDeadlineLogging;

  public SubmissionService(
      SubmissionRepository submissionRepository,
      SubmissionAuthorRepository submissionAuthorRepository,
      SubmissionFileRepository submissionFileRepository,
      DeadlineRepository deadlineRepository,
      ConferenceRepository conferenceRepository,
      StorageService storageService,
      COIService coiService,
      PCMemberRepository pcMemberRepository,
      AssignmentRepository assignmentRepository,
      UserService userService,
      TrackRepository trackRepository) {
    this.submissionRepository = submissionRepository;
    this.submissionAuthorRepository = submissionAuthorRepository;
    this.submissionFileRepository = submissionFileRepository;
    this.deadlineRepository = deadlineRepository;
    this.conferenceRepository = conferenceRepository;
    this.storageService = storageService;
    this.coiService = coiService;
    this.pcMemberRepository = pcMemberRepository;
    this.assignmentRepository = assignmentRepository;
    this.userService = userService;
    this.trackRepository = trackRepository;
  }

  /**
   * Tạo submission mới
   *
   * @param dto      Thông tin submission (title, abstract, keywords, authors)
   * @param authorId ID của author tạo submission
   * @return SubmissionResponseDTO chứa thông tin submission đã tạo
   * @throws BusinessException Nếu deadline đã qua
   */
  @Transactional
  // Tạo submission mới
  public SubmissionResponseDTO createSubmission(SubmissionCreateDTO dto, Long authorId) {
    // Check if submission deadline has passed
    checkSubmissionDeadline(dto.getConferenceId());

    // Check if author is Chair or PC (cannot submit to their own conference)
    validateAuthorRole(dto.getConferenceId(), authorId);

    Submission submission = Submission.builder()
        .conferenceId(dto.getConferenceId())
        .authorId(authorId)
        .title(dto.getTitle())
        .abstractText(dto.getAbstractText())
        .trackId(dto.getTrackId())
        .keywords(dto.getKeywords())
        .status(Submission.SubmissionStatus.DRAFT)
        .withdrawn(false)
        .build();

    Submission savedSubmission = submissionRepository.save(submission);

    // Add authors if provided
    if (dto.getAuthors() != null && !dto.getAuthors().isEmpty()) {
      final Submission finalSubmission = savedSubmission;
      List<SubmissionAuthor> authors = dto.getAuthors().stream()
          .map(
              authorDTO -> SubmissionAuthor.builder()
                  .submission(finalSubmission)
                  .userId(authorDTO.getUserId())
                  .firstName(authorDTO.getFirstName())
                  .lastName(authorDTO.getLastName())
                  .email(authorDTO.getEmail())
                  .affiliation(authorDTO.getAffiliation())
                  .isCorresponding(
                      authorDTO.getIsCorresponding() != null
                          ? authorDTO.getIsCorresponding()
                          : false)
                  .orderIndex(
                      authorDTO.getOrderIndex() != null ? authorDTO.getOrderIndex() : 0)
                  .build())
          .collect(Collectors.toList());
      submissionAuthorRepository.saveAll(authors);
    }

    // Automatic institutional COI detection for all PC members
    try {
      coiService.detectInstitutionalConflicts(savedSubmission.getId());
    } catch (Exception e) {
      logger.error("Failed to auto-detect institutional COI: {}", e.getMessage());
    }

    return mapToDTO(savedSubmission);
  }

  // Lấy chi tiết submission
  public SubmissionResponseDTO getSubmission(Long id, Long userId) {
    Submission submission = submissionRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Submission with id " + id + " not found"));

    // Check authorization
    validateSubmissionAccess(submission, userId);

    return mapToDTO(submission, userId);
  }

  // Lấy các submission của author
  public List<SubmissionResponseDTO> getMySubmissions(Long authorId) {
    List<Submission> submissions = submissionRepository.findByAuthorId(authorId);
    return mapToDTOList(submissions, authorId);
  }

  @Transactional
  // Cập nhật submission (chỉ khi DRAFT hoặc SUBMITTED; chưa được assign reviewer)
  public SubmissionResponseDTO updateSubmission(Long id, SubmissionUpdateDTO dto, Long authorId) {
    Submission submission = submissionRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Submission with id " + id + " not found"));

    // Check authorization
    if (!submission.getAuthorId().equals(authorId)) {
      throw new UnauthorizedException("You can only update your own submissions");
    }

    // Check if submission deadline has passed
    checkSubmissionDeadline(submission.getConferenceId());

    // Check if submission can be edited
    if (submission.getStatus() != Submission.SubmissionStatus.DRAFT
        && submission.getStatus() != Submission.SubmissionStatus.SUBMITTED) {
      throw new BusinessException(
          "Cannot edit submission in current status: " + submission.getStatus());
    }

    // Fix 1.2: Check if submission has been assigned to reviewers
    if (submission.getStatus() == Submission.SubmissionStatus.SUBMITTED) {
      List<Assignment> assignments = assignmentRepository.findBySubmissionId(id);
      boolean hasAcceptedAssignment = assignments.stream()
          .anyMatch(
              a -> a.getStatus() == Assignment.AssignmentStatus.ACCEPTED
                  || a.getStatus() == Assignment.AssignmentStatus.COMPLETED);
      if (hasAcceptedAssignment) {
        throw new BusinessException(
            "Cannot edit submission that has been assigned to reviewers. "
                + "Please withdraw the submission first if you need to make changes.");
      }
    }

    // Fix 1.3: Validate title and abstract are not empty after update
    if (dto.getTitle() != null) {
      String title = dto.getTitle().trim();
      if (!StringUtils.hasText(title)) {
        throw new BusinessException("Title cannot be empty");
      }
      submission.setTitle(title);
    }
    if (dto.getAbstractText() != null) {
      String abstractText = dto.getAbstractText().trim();
      if (!StringUtils.hasText(abstractText)) {
        throw new BusinessException("Abstract cannot be empty");
      }
      submission.setAbstractText(abstractText);
    }

    // Validate final state: title and abstract must not be empty
    if (submission.getTitle() == null || submission.getTitle().trim().isEmpty()) {
      throw new BusinessException("Title is required and cannot be empty");
    }
    if (submission.getAbstractText() == null || submission.getAbstractText().trim().isEmpty()) {
      throw new BusinessException("Abstract is required and cannot be empty");
    }
    if (dto.getTrackId() != null) {
      submission.setTrackId(dto.getTrackId());
    }
    if (dto.getKeywords() != null) {
      submission.setKeywords(dto.getKeywords());
    }

    // Update authors if provided
    if (dto.getAuthors() != null) {
      submissionAuthorRepository.deleteBySubmission(submission);
      final Submission finalSubmission = submission;
      List<SubmissionAuthor> authors = dto.getAuthors().stream()
          .map(
              authorDTO -> SubmissionAuthor.builder()
                  .submission(finalSubmission)
                  .userId(authorDTO.getUserId())
                  .firstName(authorDTO.getFirstName())
                  .lastName(authorDTO.getLastName())
                  .email(authorDTO.getEmail())
                  .affiliation(authorDTO.getAffiliation())
                  .isCorresponding(
                      authorDTO.getIsCorresponding() != null
                          ? authorDTO.getIsCorresponding()
                          : false)
                  .orderIndex(
                      authorDTO.getOrderIndex() != null ? authorDTO.getOrderIndex() : 0)
                  .build())
          .collect(Collectors.toList());
      submissionAuthorRepository.saveAll(authors);
    }

    Submission updatedSubmission = submissionRepository.save(submission);
    return mapToDTO(updatedSubmission);
  }

  @Transactional
  // Submit bài (chuyển trạng thái sang SUBMITTED)
  public SubmissionResponseDTO submitSubmission(Long id, Long authorId) {
    Submission submission = submissionRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Submission with id " + id + " not found"));

    // Check authorization
    if (!submission.getAuthorId().equals(authorId)) {
      throw new UnauthorizedException("You can only submit your own submissions");
    }

    // Check if submission deadline has passed
    checkSubmissionDeadline(submission.getConferenceId());

    // Check if PDF is uploaded
    if (submission.getPdfFilePath() == null || submission.getPdfFilePath().isEmpty()) {
      throw new BusinessException("PDF file is required before submission");
    }

    submission.setStatus(Submission.SubmissionStatus.SUBMITTED);
    Submission updatedSubmission = submissionRepository.save(submission);

    // Automatic institutional COI detection for all PC members
    try {
      coiService.detectInstitutionalConflicts(updatedSubmission.getId());
    } catch (Exception e) {
      logger.error("Failed to auto-detect institutional COI: {}", e.getMessage());
    }

    return mapToDTO(updatedSubmission);
  }

  @Transactional
  // Rút bài (withdraw)
  public SubmissionResponseDTO withdrawSubmission(Long id, Long authorId) {
    Submission submission = submissionRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Submission with id " + id + " not found"));

    // Check authorization
    if (!submission.getAuthorId().equals(authorId)) {
      throw new UnauthorizedException("You can only withdraw your own submissions");
    }

    // Check if submission deadline has passed
    checkSubmissionDeadline(submission.getConferenceId());

    // Check if submission can be withdrawn
    if (submission.getStatus() == Submission.SubmissionStatus.ACCEPTED
        || submission.getStatus() == Submission.SubmissionStatus.CAMERA_READY) {
      throw new BusinessException("Cannot withdraw accepted submissions");
    }

    submission.setWithdrawn(true);
    Submission updatedSubmission = submissionRepository.save(submission);

    return mapToDTO(updatedSubmission);
  }

  @Transactional
  // Upload file PDF cho bài báo
  public SubmissionFileDTO uploadPdf(Long submissionId, MultipartFile file, Long authorId)
      throws IOException {
    Submission submission = submissionRepository
        .findById(submissionId)
        .orElseThrow(
            () -> new NotFoundException("Submission with id " + submissionId + " not found"));

    // Check authorization
    if (!submission.getAuthorId().equals(authorId)) {
      throw new UnauthorizedException("You can only upload files for your own submissions");
    }

    // Check if submission deadline has passed
    checkSubmissionDeadline(submission.getConferenceId());

    // Store file using StorageService (validation is done inside)
    String relativePath = storageService.storeSubmissionPdf(submission.getConferenceId(), submissionId, file);

    // Calculate checksum
    String checksum = calculateChecksumFromStream(storageService.getFileStream(relativePath));

    // Get next version number
    Integer versionNumber = submissionFileRepository.countBySubmission(submission) + 1;

    // Mark previous files as not current
    submissionFileRepository
        .findBySubmissionAndIsCurrentTrue(submission)
        .ifPresent(
            prevFile -> {
              prevFile.setIsCurrent(false);
              submissionFileRepository.save(prevFile);
            });

    // Create submission file record
    SubmissionFile submissionFile = SubmissionFile.builder()
        .submission(submission)
        .versionNumber(versionNumber)
        .filePath(relativePath)
        .fileName(file.getOriginalFilename())
        .fileSize(file.getSize())
        .contentType(file.getContentType())
        .checksum(checksum)
        .isCurrent(true)
        .build();

    SubmissionFile savedFile = submissionFileRepository.save(submissionFile);

    // Update submission PDF path
    submission.setPdfFilePath(relativePath);
    submissionRepository.save(submission);

    return mapFileToDTO(savedFile);
  }

  /**
   * Kiểm tra deadline cho submission
   *
   * <p>
   * Fix 1.1: Cải thiện logic deadline với grace period và logging
   *
   * <ul>
   * <li>Hard deadline: Chặn hoàn toàn sau deadline
   * <li>Soft deadline: Cho phép trong grace period (mặc định 24 giờ)
   * <li>Logging khi bypass deadline (nếu enable)
   * </ul>
   *
   * @param conferenceId ID của conference
   * @throws BusinessException Nếu deadline đã qua và không có grace period
   */
  private void checkSubmissionDeadline(Long conferenceId) {
    List<Deadline> deadlines = deadlineRepository.findByConferenceId(conferenceId);
    Deadline submissionDeadline = deadlines.stream()
        .filter(d -> d.getType() == DeadlineType.SUBMISSION)
        .findFirst()
        .orElse(null);

    if (submissionDeadline == null) {
      // Không có deadline, cho phép
      return;
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime dueDate = submissionDeadline.getDueDate();

    if (dueDate.isBefore(now)) {
      // Deadline đã qua
      if (submissionDeadline.getHardDeadline()) {
        // Hard deadline: Chặn hoàn toàn
        throw new BusinessException("Submission deadline has passed");
      } else {
        // Soft deadline: Kiểm tra grace period
        LocalDateTime gracePeriodEnd = dueDate.plusHours(gracePeriodHours);
        if (now.isAfter(gracePeriodEnd)) {
          // Đã qua cả grace period
          if (enableDeadlineLogging) {
            logger.warn(
                "Submission deadline passed for conference {}: deadline={}, now={}, gracePeriodEnd={}",
                conferenceId,
                dueDate,
                now,
                gracePeriodEnd);
          }
          throw new BusinessException(
              String.format(
                  "Submission deadline has passed. Grace period of %d hours has also expired.",
                  gracePeriodHours));
        } else {
          // Vẫn trong grace period, cho phép nhưng log warning
          if (enableDeadlineLogging) {
            logger.warn(
                "Submission operation allowed within grace period for conference {}: deadline={}, now={}, gracePeriodEnd={}",
                conferenceId,
                dueDate,
                now,
                gracePeriodEnd);
          }
          // Cho phép thao tác trong grace period
        }
      }
    }
  }

  /**
   * Kiểm tra nếu author là Chair hoặc PC member của hội nghị
   * Chair và PC member (đã ACCEPTED) không được phép nộp bài vào chính hội nghị
   * đó
   *
   * @param conferenceId ID của conference
   * @param authorId     ID của author
   * @throws BusinessException Nếu author là Chair hoặc PC
   */
  // Validate quyền nộp bài (không phải Chair/PC)
  private void validateAuthorRole(Long conferenceId, Long authorId) {
    // 1. Kiểm tra nếu là Chair
    com.uth.confms.conference.entity.Conference conference = conferenceRepository.findById(conferenceId)
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    if (conference.getChairId().equals(authorId)) {
      throw new BusinessException("Bạn không được phép nộp bài vào hội nghị mà bạn đang làm Chair");
    }

    // 2. Kiểm tra nếu là PC Member đã chấp nhận
    java.util.Optional<com.uth.confms.pc.entity.PCMember> pcMember = pcMemberRepository
        .findByConferenceIdAndUserId(conferenceId, authorId);
    if (pcMember.isPresent()
        && pcMember.get().getStatus() == com.uth.confms.pc.entity.PCMember.PCMemberStatus.ACCEPTED) {
      throw new BusinessException("Bạn không được phép nộp bài vào hội nghị mà bạn đã chấp nhận tham gia hội đồng PC");
    }
  }

  /**
   * Tính checksum từ InputStream
   *
   * @param inputStream InputStream của file
   * @return MD5 checksum dạng hex string
   */
  private String calculateChecksumFromStream(InputStream inputStream) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] buffer = new byte[8192];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        md.update(buffer, 0, bytesRead);
      }
      byte[] digest = md.digest();
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException("Error calculating checksum", e);
    } finally {
      try {
        inputStream.close();
      } catch (IOException e) {
        // Ignore
      }
    }
  }

  private SubmissionResponseDTO mapToDTO(Submission submission) {
    return mapToDTO(submission, null);
  }

  private SubmissionResponseDTO mapToDTO(Submission submission, Long currentUserId) {
    List<SubmissionAuthorDTO> authors = submissionAuthorRepository.findBySubmission(submission).stream()
        .map(this::mapAuthorToDTO)
        .collect(Collectors.toList());

    List<SubmissionFileDTO> files = submissionFileRepository.findBySubmission(submission).stream()
        .map(this::mapFileToDTO)
        .collect(Collectors.toList());

    Conference conference = conferenceRepository.findById(submission.getConferenceId()).orElse(null);
    String reviewMode = conference != null && conference.getReviewMode() != null ? conference.getReviewMode().name()
        : "DOUBLE_BLIND";

    // Double-blind protection: Blur authors if caller is a reviewer (and not
    // chair/admin/author)
    if (currentUserId != null && conference != null
        && conference.getReviewMode() == Conference.ReviewMode.DOUBLE_BLIND) {
      boolean isAuthor = submission.getAuthorId().equals(currentUserId);
      boolean isChair = conference.getChairId().equals(currentUserId);
      boolean isAdmin = userService.hasRole(currentUserId, "ADMIN");

      if (!isAuthor && !isChair && !isAdmin) {
        // Blur authors for reviewers
        authors = authors.stream()
            .map(a -> SubmissionAuthorDTO.builder()
                .firstName("Anonymous")
                .lastName("Author")
                .email("hidden@example.com")
                .affiliation("Hidden for Review")
                .isCorresponding(a.getIsCorresponding())
                .orderIndex(a.getOrderIndex())
                .build())
            .collect(Collectors.toList());
      }
    }

    boolean canEdit = submission.getStatus() == Submission.SubmissionStatus.DRAFT ||
        submission.getStatus() == Submission.SubmissionStatus.SUBMITTED;

    boolean canWithdraw = !submission.getWithdrawn() &&
        submission.getStatus() != Submission.SubmissionStatus.ACCEPTED &&
        submission.getStatus() != Submission.SubmissionStatus.CAMERA_READY;

    String trackName = null;
    if (submission.getTrackId() != null) {
      trackName = trackRepository.findById(submission.getTrackId())
          .map(com.uth.confms.conference.entity.Track::getName)
          .orElse(null);
    }

    return SubmissionResponseDTO.builder()
        .id(submission.getId())
        .conferenceId(submission.getConferenceId())
        .authorId(submission.getAuthorId())
        .title(submission.getTitle())
        .abstractText(submission.getAbstractText())
        .status(submission.getStatus().name())
        .pdfFilePath(submission.getPdfFilePath())
        .trackId(submission.getTrackId())
        .keywords(submission.getKeywords())
        .withdrawn(submission.getWithdrawn())
        .authors(authors)
        .files(files)
        .createdAt(submission.getCreatedAt())
        .updatedAt(submission.getUpdatedAt())
        .canEdit(canEdit)
        .canWithdraw(canWithdraw)
        .reviewMode(reviewMode)
        .trackName(trackName)
        .build();
  }

  private List<SubmissionResponseDTO> mapToDTOList(List<Submission> submissions, Long currentUserId) {
    if (submissions.isEmpty()) {
      return List.of();
    }

    List<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());

    // Batch fetch authors and files
    Map<Long, List<SubmissionAuthor>> authorsMap = submissionAuthorRepository.findBySubmissionIdIn(submissionIds)
        .stream().collect(Collectors.groupingBy(a -> a.getSubmission().getId()));

    Map<Long, List<SubmissionFile>> filesMap = submissionFileRepository.findBySubmissionIdIn(submissionIds)
        .stream().collect(Collectors.groupingBy(f -> f.getSubmission().getId()));

    Set<Long> trackIds = submissions.stream()
        .map(Submission::getTrackId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    Map<Long, String> trackNameMap = trackRepository.findAllById(trackIds).stream()
        .collect(Collectors.toMap(com.uth.confms.conference.entity.Track::getId,
            com.uth.confms.conference.entity.Track::getName));

    // Get conference details once if needed (for double-blind check)
    // For simplicity, we assume all submissions in the list belong to the same
    // conference or we fetch them as needed.
    // In getSubmissionsByConference, they are all from the same conference.
    Map<Long, Conference> conferenceMap = new HashMap<>();

    // Optimize: Check admin role once for the current user
    boolean isGlobalAdmin = currentUserId != null && userService.hasRole(currentUserId, "ADMIN");

    return submissions.stream().map(submission -> {
      List<SubmissionAuthorDTO> authors = authorsMap.getOrDefault(submission.getId(), List.of()).stream()
          .map(this::mapAuthorToDTO).collect(Collectors.toList());

      List<SubmissionFileDTO> files = filesMap.getOrDefault(submission.getId(), List.of()).stream()
          .map(this::mapFileToDTO).collect(Collectors.toList());

      Conference conference = conferenceMap.computeIfAbsent(submission.getConferenceId(),
          id -> conferenceRepository.findById(id).orElse(null));

      String reviewMode = conference != null && conference.getReviewMode() != null ? conference.getReviewMode().name()
          : "DOUBLE_BLIND";

      // Double-blind protection
      if (currentUserId != null && conference != null
          && conference.getReviewMode() == Conference.ReviewMode.DOUBLE_BLIND) {
        boolean isAuthor = submission.getAuthorId().equals(currentUserId);
        boolean isChair = conference.getChairId().equals(currentUserId);

        if (!isAuthor && !isChair && !isGlobalAdmin) {
          authors = authors.stream()
              .map(a -> SubmissionAuthorDTO.builder()
                  .firstName("Anonymous").lastName("Author").email("hidden@example.com")
                  .affiliation("Hidden for Review").isCorresponding(a.getIsCorresponding())
                  .orderIndex(a.getOrderIndex()).build())
              .collect(Collectors.toList());
        }
      }

      boolean canEdit = submission.getStatus() == Submission.SubmissionStatus.DRAFT ||
          submission.getStatus() == Submission.SubmissionStatus.SUBMITTED;
      boolean canWithdraw = !submission.getWithdrawn() &&
          submission.getStatus() != Submission.SubmissionStatus.ACCEPTED &&
          submission.getStatus() != Submission.SubmissionStatus.CAMERA_READY;

      return SubmissionResponseDTO.builder()
          .id(submission.getId()).conferenceId(submission.getConferenceId())
          .authorId(submission.getAuthorId()).title(submission.getTitle())
          .abstractText(submission.getAbstractText()).status(submission.getStatus().name())
          .pdfFilePath(submission.getPdfFilePath()).trackId(submission.getTrackId())
          .keywords(submission.getKeywords()).withdrawn(submission.getWithdrawn())
          .authors(authors).files(files).createdAt(submission.getCreatedAt())
          .updatedAt(submission.getUpdatedAt()).canEdit(canEdit).canWithdraw(canWithdraw)
          .reviewMode(reviewMode).trackName(trackNameMap.get(submission.getTrackId())).build();
    }).collect(Collectors.toList());
  }

  private SubmissionAuthorDTO mapAuthorToDTO(SubmissionAuthor author) {
    return SubmissionAuthorDTO.builder()
        .id(author.getId())
        .userId(author.getUserId())
        .firstName(author.getFirstName())
        .lastName(author.getLastName())
        .email(author.getEmail())
        .affiliation(author.getAffiliation())
        .isCorresponding(author.getIsCorresponding())
        .orderIndex(author.getOrderIndex())
        .build();
  }

  private SubmissionFileDTO mapFileToDTO(SubmissionFile file) {
    return SubmissionFileDTO.builder()
        .id(file.getId())
        .versionNumber(file.getVersionNumber())
        .fileName(file.getFileName())
        .filePath(file.getFilePath())
        .fileSize(file.getFileSize())
        .contentType(file.getContentType())
        .isCurrent(file.getIsCurrent())
        .uploadedAt(file.getUploadedAt())
        .uploadNote(file.getUploadNote())
        .build();
  }

  /**
   * Helper method to validate if a user (userId) has access to view/download
   * submission
   */
  private void validateSubmissionAccess(Submission submission, Long userId) {
    logger.debug("DEBUG: validateSubmissionAccess for submissionId={}, userId={}", submission.getId(), userId);
    if (submission.getAuthorId().equals(userId)) {
      logger.debug("DEBUG: Access granted - IS AUTHOR");
      return;
    }

    // Check if Chair
    boolean isChair = conferenceRepository
        .findById(submission.getConferenceId())
        .map(c -> c.getChairId().equals(userId))
        .orElse(false);
    if (isChair) {
      logger.debug("DEBUG: Access granted - IS CHAIR");
      return;
    }

    // Check if Assigned Reviewer
    List<Assignment> assignments = assignmentRepository.findBySubmissionId(submission.getId());
    logger.debug("DEBUG: Found assignments: {}", assignments.size());
    boolean isAssignedReviewer = assignments.stream()
        .anyMatch(a -> {
          boolean match = a.getReviewerId().equals(userId);
          if (match)
            logger.debug("DEBUG: Match found in assignment: {}", a.getId());
          return match;
        });
    if (isAssignedReviewer) {
      logger.debug("DEBUG: Access granted - IS ASSIGNED REVIEWER");
      return;
    }

    logger.warn("DEBUG: Access DENIED for userId: {} on submissionId: {}", userId, submission.getId());
    throw new UnauthorizedException("You do not have permission to access these resources");
  }

  /**
   * Download PDF file hiện tại của submission
   *
   * @param id     ID của submission
   * @param userId ID của user requesting
   * @return InputStream của file PDF
   * @throws NotFoundException     Nếu submission không tồn tại
   * @throws UnauthorizedException Nếu user không có quyền truy cập
   * @throws BusinessException     Nếu submission chưa có PDF file
   */
  public InputStream downloadPdfFile(Long id, Long userId) {
    Submission submission = submissionRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Submission with id " + id + " not found"));

    // Check authorization
    validateSubmissionAccess(submission, userId);

    // Check if PDF file exists
    if (submission.getPdfFilePath() == null || submission.getPdfFilePath().isEmpty()) {
      throw new BusinessException("Submission does not have a PDF file");
    }

    return storageService.getFileStream(submission.getPdfFilePath());
  }

  /**
   * Lấy danh sách tất cả các version của PDF file đã upload
   *
   * @param id     ID của submission
   * @param userId ID của user requesting
   * @return Danh sách SubmissionFileDTO
   * @throws NotFoundException     Nếu submission không tồn tại
   * @throws UnauthorizedException Nếu user không có quyền truy cập
   */
  // Lấy danh sách các version file cũ
  public List<SubmissionFileDTO> getFileVersions(Long id, Long userId) {
    Submission submission = submissionRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Submission with id " + id + " not found"));

    // Check authorization
    validateSubmissionAccess(submission, userId);

    return submissionFileRepository.findBySubmission(submission).stream()
        .map(this::mapFileToDTO)
        .collect(Collectors.toList());
  }

  /**
   * Download một version cụ thể của PDF file
   *
   * @param submissionId ID của submission
   * @param fileId       ID của file version
   * @param userId       ID của user requesting
   * @return InputStream của file PDF
   * @throws NotFoundException     Nếu submission hoặc file không tồn tại
   * @throws UnauthorizedException Nếu user không có quyền truy cập
   */
  public InputStream downloadFileVersion(Long submissionId, Long fileId, Long userId) {
    Submission submission = submissionRepository
        .findById(submissionId)
        .orElseThrow(
            () -> new NotFoundException(
                "Submission with id " + submissionId + " not found"));

    // Check authorization
    validateSubmissionAccess(submission, userId);

    SubmissionFile file = submissionFileRepository
        .findById(fileId)
        .orElseThrow(() -> new NotFoundException("File with id " + fileId + " not found"));

    // Verify file belongs to submission
    if (!file.getSubmission().getId().equals(submissionId)) {
      throw new BusinessException("File does not belong to this submission");
    }

    return storageService.getFileStream(file.getFilePath());
  }

  /**
   * Xóa submission (chỉ cho phép xóa draft chưa submit)
   *
   * @param id       ID của submission
   * @param authorId ID của author
   * @throws NotFoundException     Nếu submission không tồn tại
   * @throws UnauthorizedException Nếu author không có quyền truy cập
   * @throws BusinessException     Nếu submission không thể xóa (đã submit hoặc đã
   *                               được review)
   */
  @Transactional
  public void deleteSubmission(Long id, Long authorId) {
    Submission submission = submissionRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Submission with id " + id + " not found"));

    // Check authorization
    if (!submission.getAuthorId().equals(authorId)) {
      throw new UnauthorizedException("You can only delete your own submissions");
    }

    // Only allow deletion of DRAFT submissions
    if (submission.getStatus() != Submission.SubmissionStatus.DRAFT) {
      throw new BusinessException(
          "Cannot delete submission. Only DRAFT submissions can be deleted.");
    }

    // Delete associated files
    List<SubmissionFile> files = submissionFileRepository.findBySubmission(submission);
    for (SubmissionFile file : files) {
      if (file.getFilePath() != null) {
        storageService.deleteFile(file.getFilePath());
      }
      submissionFileRepository.delete(file);
    }

    // Delete associated authors
    submissionAuthorRepository.deleteBySubmission(submission);

    // Delete submission
    submissionRepository.delete(submission);
  }

  /**
   * Lấy danh sách submissions của một conference (CHAIR/ADMIN only)
   *
   * <p>
   * Endpoint này được bảo vệ bởi @PreAuthorize("hasRole('CHAIR') or
   * hasRole('ADMIN')"),
   * nên chỉ CHAIR hoặc ADMIN mới có thể gọi. Nếu user không phải là chair của
   * conference,
   * thì chắc chắn là ADMIN (vì đã pass qua @PreAuthorize).
   *
   * @param conferenceId ID của conference
   * @param userId       ID của user (chair hoặc admin)
   * @return Danh sách SubmissionResponseDTO
   * @throws NotFoundException     Nếu conference không tồn tại
   * @throws UnauthorizedException Nếu user không phải là chair của conference và
   *                               không phải admin
   */
  public List<SubmissionResponseDTO> getSubmissionsByConference(Long conferenceId, Long userId) {
    // Check if conference exists
    var conference = conferenceRepository
        .findById(conferenceId)
        .orElseThrow(() -> new NotFoundException("Conference with id " + conferenceId + " not found"));

    // Check authorization - only chair of conference or admin can view submissions
    // Since endpoint is protected by @PreAuthorize("hasRole('CHAIR') or
    // hasRole('ADMIN')"),
    // if user is not chair, they must be admin
    if (!conference.getChairId().equals(userId)) {
      // User is not chair, check if they are admin
      // Since @PreAuthorize already ensures CHAIR or ADMIN, we allow access here
      // Admin can view submissions of any conference
      logger.info("User {} (not chair) accessing submissions for conference {}", userId, conferenceId);
    }

    // Get all submissions for this conference
    List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);

    // Map to DTOs
    return mapToDTOList(submissions, userId);
  }
}
