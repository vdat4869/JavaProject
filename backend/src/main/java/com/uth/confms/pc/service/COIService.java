package com.uth.confms.pc.service;

import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.auth.service.AuditLogService;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.pc.dto.COIHistoryDTO;
import com.uth.confms.pc.dto.COIDeclareDTO;
import com.uth.confms.pc.dto.COIStatisticsDTO;
import com.uth.confms.pc.entity.ConflictOfInterest;
import com.uth.confms.pc.repository.ConflictOfInterestRepository;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.entity.SubmissionAuthor;
import com.uth.confms.submission.repository.SubmissionAuthorRepository;
import com.uth.confms.submission.repository.SubmissionRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý Conflict of Interest (COI) declarations
 *
 * <p>Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 *   <li>Khai báo COI giữa reviewer và submission
 *   <li>Tự động phát hiện COI (nếu reviewer là author)
 *   <li>Kiểm tra COI trước khi assign review
 *   <li>Quản lý COI records
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class COIService {
  private final ConflictOfInterestRepository coiRepository;
  private final SubmissionRepository submissionRepository;
  private final SubmissionAuthorRepository submissionAuthorRepository;
  private final UserRepository userRepository;
  private final ConferenceRepository conferenceRepository;
  private final AuditLogService auditLogService;

  public COIService(
      ConflictOfInterestRepository coiRepository,
      SubmissionRepository submissionRepository,
      SubmissionAuthorRepository submissionAuthorRepository,
      UserRepository userRepository,
      ConferenceRepository conferenceRepository,
      AuditLogService auditLogService) {
    this.coiRepository = coiRepository;
    this.submissionRepository = submissionRepository;
    this.submissionAuthorRepository = submissionAuthorRepository;
    this.userRepository = userRepository;
    this.conferenceRepository = conferenceRepository;
    this.auditLogService = auditLogService;
  }

  @Transactional
  public ConflictOfInterest declareCOI(COIDeclareDTO dto, Long reviewerId, HttpServletRequest request) {
    // Validate submission exists
    submissionRepository
        .findById(dto.getSubmissionId())
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Submission with id " + dto.getSubmissionId() + " not found"));

    // Check if COI already exists
    coiRepository
        .findByReviewerIdAndSubmissionId(reviewerId, dto.getSubmissionId())
        .ifPresent(
            existing -> {
              if (existing.getActive()) {
                throw new BusinessException("COI already declared for this submission");
              }
            });

    ConflictOfInterest coi =
        ConflictOfInterest.builder()
            .reviewerId(reviewerId)
            .submissionId(dto.getSubmissionId())
            .type(ConflictOfInterest.COIType.valueOf(dto.getType()))
            .reason(dto.getReason())
            .active(true)
            .build();

    coi = coiRepository.save(coi);

    // Audit log
    User reviewer = userRepository.findById(reviewerId).orElse(null);
    auditLogService.logAction(
        reviewerId,
        reviewer != null ? reviewer.getEmail() : null,
        "COI_DECLARED",
        "COI",
        coi.getId(),
        String.format(
            "COI declared: type=%s, submissionId=%d, reason=%s",
            dto.getType(), dto.getSubmissionId(), dto.getReason()),
        request);

    return coi;
  }

  @Transactional
  public ConflictOfInterest declareCOI(COIDeclareDTO dto, Long reviewerId) {
    return declareCOI(dto, reviewerId, null);
  }

  @Transactional
  public void removeCOI(Long coiId, Long reviewerId, HttpServletRequest request) {
    ConflictOfInterest coi =
        coiRepository
            .findById(coiId)
            .orElseThrow(() -> new NotFoundException("COI record not found"));

    // Check authorization
    if (!coi.getReviewerId().equals(reviewerId)) {
      throw new BusinessException("You can only remove your own COI declarations");
    }

    coi.setActive(false);
    coiRepository.save(coi);

    // Audit log
    User reviewer = userRepository.findById(reviewerId).orElse(null);
    auditLogService.logAction(
        reviewerId,
        reviewer != null ? reviewer.getEmail() : null,
        "COI_REMOVED",
        "COI",
        coiId,
        String.format("COI removed: submissionId=%d", coi.getSubmissionId()),
        request);
  }

  @Transactional
  public void removeCOI(Long coiId, Long reviewerId) {
    removeCOI(coiId, reviewerId, null);
  }

  public List<ConflictOfInterest> getCOIsByReviewer(Long reviewerId) {
    return coiRepository.findByReviewerIdAndActiveTrue(reviewerId);
  }

  public List<ConflictOfInterest> getCOIsBySubmission(Long submissionId) {
    return coiRepository.findBySubmissionIdAndActiveTrue(submissionId);
  }

  public boolean hasCOI(Long reviewerId, Long submissionId) {
    return coiRepository
        .findByReviewerIdAndSubmissionId(reviewerId, submissionId)
        .map(ConflictOfInterest::getActive)
        .orElse(false);
  }

  @Transactional
  public void detectAndSuggestCOI(Long reviewerId, Long submissionId) {
    Submission submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(() -> new NotFoundException("Submission not found"));

    // Check if reviewer is an author of the submission
    List<SubmissionAuthor> authors = submissionAuthorRepository.findBySubmission(submission);
    boolean isAuthor = authors.stream().anyMatch(author -> author.getUserId().equals(reviewerId));

    if (isAuthor) {
      // Auto-declare COI
      if (!hasCOI(reviewerId, submissionId)) {
        ConflictOfInterest coi =
            ConflictOfInterest.builder()
                .reviewerId(reviewerId)
                .submissionId(submissionId)
                .type(ConflictOfInterest.COIType.CO_AUTHOR)
                .reason("Reviewer is an author of this submission")
                .active(true)
                .build();
        coi = coiRepository.save(coi);

        // Audit log for auto-detection
        User reviewer = userRepository.findById(reviewerId).orElse(null);
        auditLogService.logAction(
            reviewerId,
            reviewer != null ? reviewer.getEmail() : null,
            "COI_AUTO_DETECTED",
            "COI",
            coi.getId(),
            String.format(
                "COI auto-detected: type=CO_AUTHOR, submissionId=%d", submissionId),
            null);
      }
    }
  }

  /**
   * Lấy COI history cho một conference
   *
   * @param conferenceId ID của conference
   * @param chairId ID của chair (for authorization)
   * @return List of COIHistoryDTO
   */
  public List<COIHistoryDTO> getCOIHistory(Long conferenceId, Long chairId) {
    Conference conference =
        conferenceRepository
            .findById(conferenceId)
            .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can view COI history");
    }

    // Get all submissions for this conference
    List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
    List<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());

    // Get all COIs for these submissions
    List<ConflictOfInterest> allCOIs =
        coiRepository.findAll().stream()
            .filter(coi -> submissionIds.contains(coi.getSubmissionId()))
            .collect(Collectors.toList());

    // Map to COIHistoryDTO
    return allCOIs.stream()
        .map(
            coi -> {
              User reviewer = userRepository.findById(coi.getReviewerId()).orElse(null);
              Submission submission =
                  submissionRepository.findById(coi.getSubmissionId()).orElse(null);

              // Determine action type
              String action = "DECLARED";
              if (!coi.getActive()) {
                action = "REMOVED";
              } else if (coi.getReason() != null
                  && coi.getReason().contains("auto-detected")
                  || coi.getReason().contains("Reviewer is an author")) {
                action = "AUTO_DETECTED";
              }

              return COIHistoryDTO.builder()
                  .id(coi.getId())
                  .reviewerId(coi.getReviewerId())
                  .reviewerEmail(reviewer != null ? reviewer.getEmail() : null)
                  .reviewerName(reviewer != null ? reviewer.getFullName() : null)
                  .submissionId(coi.getSubmissionId())
                  .submissionTitle(submission != null ? submission.getTitle() : null)
                  .coiType(coi.getType().name())
                  .reason(coi.getReason())
                  .active(coi.getActive())
                  .declaredAt(coi.getDeclaredAt())
                  .action(action)
                  .build();
            })
        .sorted((a, b) -> b.getDeclaredAt().compareTo(a.getDeclaredAt())) // Most recent first
        .collect(Collectors.toList());
  }

  /**
   * Lấy COI statistics cho một conference
   *
   * @param conferenceId ID của conference
   * @param chairId ID của chair (for authorization)
   * @return COIStatisticsDTO
   */
  public COIStatisticsDTO getCOIStatistics(Long conferenceId, Long chairId) {
    Conference conference =
        conferenceRepository
            .findById(conferenceId)
            .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can view COI statistics");
    }

    // Get all submissions for this conference
    List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
    List<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());

    // Get all COIs for these submissions
    List<ConflictOfInterest> allCOIs =
        coiRepository.findAll().stream()
            .filter(coi -> submissionIds.contains(coi.getSubmissionId()))
            .collect(Collectors.toList());

    int totalCOIs = allCOIs.size();
    int activeCOIs = (int) allCOIs.stream().filter(ConflictOfInterest::getActive).count();
    int inactiveCOIs = totalCOIs - activeCOIs;

    // COI distribution by type
    Map<String, Long> coiByType = new HashMap<>();
    for (ConflictOfInterest coi : allCOIs) {
      if (coi.getActive()) {
        String type = coi.getType().name();
        coiByType.put(type, coiByType.getOrDefault(type, 0L) + 1);
      }
    }

    // Unique reviewers with COIs
    Set<Long> reviewersWithCOIsSet =
        allCOIs.stream()
            .filter(ConflictOfInterest::getActive)
            .map(ConflictOfInterest::getReviewerId)
            .collect(Collectors.toSet());
    int reviewersWithCOIs = reviewersWithCOIsSet.size();

    // Unique submissions with COIs
    Set<Long> submissionsWithCOIsSet =
        allCOIs.stream()
            .filter(ConflictOfInterest::getActive)
            .map(ConflictOfInterest::getSubmissionId)
            .collect(Collectors.toSet());
    int submissionsWithCOIs = submissionsWithCOIsSet.size();

    // Recent COIs (last 30 days)
    LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
    int recentCOIs =
        (int)
            allCOIs.stream()
                .filter(coi -> coi.getDeclaredAt().isAfter(thirtyDaysAgo))
                .count();

    return COIStatisticsDTO.builder()
        .conferenceId(conferenceId)
        .conferenceName(conference.getName())
        .totalCOIs(totalCOIs)
        .activeCOIs(activeCOIs)
        .inactiveCOIs(inactiveCOIs)
        .coiByType(coiByType)
        .reviewersWithCOIs(reviewersWithCOIs)
        .submissionsWithCOIs(submissionsWithCOIs)
        .recentCOIs(recentCOIs)
        .build();
  }
}
