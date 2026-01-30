package com.uth.confms.decision.service;

import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.decision.dto.BulkDecisionRequestDTO;
import com.uth.confms.decision.dto.DecisionHistoryDTO;
import com.uth.confms.decision.dto.DecisionRequestDTO;
import com.uth.confms.decision.dto.DecisionResultDTO;
import com.uth.confms.decision.dto.ReviewSummaryDTO;
import com.uth.confms.decision.entity.Decision;
import com.uth.confms.decision.entity.DecisionHistory;
import com.uth.confms.decision.repository.DecisionHistoryRepository;
import com.uth.confms.decision.repository.DecisionRepository;
import com.uth.confms.review.entity.Review;
import com.uth.confms.review.repository.ReviewRepository;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.repository.SubmissionRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý decisions (quyết định Accept/Reject)
 *
 * <p>
 * Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 * <li>Tạo decision cho submission (chỉ chair)
 * <li>Auto-update submission status
 * <li>Quản lý pending notifications
 * <li>Validation: submission phải ở status UNDER_REVIEW
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class DecisionService {
  private final DecisionRepository decisionRepository;
  private final SubmissionRepository submissionRepository;
  private final ConferenceRepository conferenceRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;
  private final ReviewRepository reviewRepository;
  private final DecisionHistoryRepository decisionHistoryRepository;

  public DecisionService(
      DecisionRepository decisionRepository,
      SubmissionRepository submissionRepository,
      ConferenceRepository conferenceRepository,
      UserRepository userRepository,
      NotificationService notificationService,
      ReviewRepository reviewRepository,
      DecisionHistoryRepository decisionHistoryRepository) {
    this.decisionRepository = decisionRepository;
    this.submissionRepository = submissionRepository;
    this.conferenceRepository = conferenceRepository;
    this.userRepository = userRepository;
    this.notificationService = notificationService;
    this.reviewRepository = reviewRepository;
    this.decisionHistoryRepository = decisionHistoryRepository;
  }

  @Transactional
  // Thực hiện decision cho nhiều submission cùng lúc
  public List<DecisionResultDTO> makeBulkDecisions(BulkDecisionRequestDTO dto, Long chairId, boolean isAdmin) {
    return dto.getSubmissionIds().stream()
        .map(subId -> {
          DecisionRequestDTO request = new DecisionRequestDTO();
          request.setSubmissionId(subId);
          request.setType(dto.getType());
          request.setComments(dto.getComments());
          request.setSendNotification(dto.getSendNotification());
          return makeDecision(request, chairId, isAdmin);
        })
        .collect(Collectors.toList());
  }

  // Lấy lịch sử thay đổi decision
  public List<DecisionHistoryDTO> getDecisionHistoryDTOs(Long decisionId) {
    return decisionHistoryRepository.findByDecisionIdOrderByChangedAtDesc(decisionId).stream()
        .map(h -> {
          User changer = userRepository.findById(h.getChangedBy()).orElse(null);
          return new DecisionHistoryDTO(
              h.getId(),
              h.getDecisionId(),
              h.getChangedBy(),
              changer != null ? changer.getFullName() : "Unknown",
              h.getChangeType().name(),
              h.getOldValue(),
              h.getNewValue(),
              h.getFieldName(),
              h.getDescription(),
              h.getChangedAt());
        })
        .collect(Collectors.toList());
  }

  @Transactional
  // Tạo hoặc cập nhật decision cho một submission
  public DecisionResultDTO makeDecision(DecisionRequestDTO dto, Long chairId, boolean isAdmin) {
    Submission submission = submissionRepository
        .findById(dto.getSubmissionId())
        .orElseThrow(
            () -> new NotFoundException(
                "Submission with id " + dto.getSubmissionId() + " not found"));

    Conference conference = conferenceRepository
        .findById(submission.getConferenceId())
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization - only chair (or admin) can make decisions
    if (!isAdmin && !conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair or admin can make decisions");
    }

    // Check if submission is in review, submitted or reviewed
    if (submission.getStatus() != Submission.SubmissionStatus.UNDER_REVIEW
        && submission.getStatus() != Submission.SubmissionStatus.SUBMITTED
        && submission.getStatus() != Submission.SubmissionStatus.REVIEWED) {
      throw new BusinessException("Submission must be under review, submitted or reviewed before making decision");
    }

    // Check if decision already exists
    decisionRepository
        .findBySubmissionId(dto.getSubmissionId())
        .ifPresent(
            existing -> {
              if (existing.getLocked() != null && existing.getLocked()) {
                throw new BusinessException(
                    "Cannot modify decision: Decision has been locked after notification");
              }
              throw new BusinessException("Decision already exists for this submission");
            });

    // Validate decision type
    Decision.DecisionType decisionType;
    try {
      decisionType = Decision.DecisionType.valueOf(dto.getType());
    } catch (IllegalArgumentException e) {
      throw new BusinessException("Invalid decision type: " + dto.getType());
    }

    // Create decision
    Decision decision = Decision.builder()
        .submissionId(dto.getSubmissionId())
        .decidedBy(chairId)
        .type(decisionType)
        .comments(dto.getComments())
        .notified(false)
        .build();

    decision = decisionRepository.save(decision);

    // Log decision creation in history
    logDecisionChange(
        decision.getId(),
        chairId,
        DecisionHistory.ChangeType.CREATED,
        null,
        null,
        "type",
        "Decision created: " + decisionType.name());

    // Update submission status
    if (decisionType == Decision.DecisionType.ACCEPT
        || decisionType == Decision.DecisionType.CONDITIONAL_ACCEPT) {
      submission.setStatus(Submission.SubmissionStatus.ACCEPTED);
    } else {
      submission.setStatus(Submission.SubmissionStatus.REJECTED);
    }
    submissionRepository.save(submission);

    // Send notification if requested
    if (dto.getSendNotification() != null && dto.getSendNotification()) {
      notificationService.sendDecisionNotification(decision);
    }

    return mapToDTO(decision);
  }

  // Lấy decision của một submission
  public DecisionResultDTO getDecisionBySubmission(Long submissionId) {
    Decision decision = decisionRepository
        .findBySubmissionId(submissionId)
        .orElseThrow(() -> new NotFoundException("Decision not found for this submission"));

    return mapToDTO(decision);
  }

  // Lấy danh sách decision của hội nghị
  public List<DecisionResultDTO> getDecisionsByConference(Long conferenceId, Long chairId, boolean isAdmin) {
    Conference conference = conferenceRepository
        .findById(conferenceId)
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization
    if (!isAdmin && !conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair or admin can view decisions");
    }

    // Get all submissions for this conference
    List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
    List<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());

    // Batch fetch decisions
    Map<Long, Decision> decisionMap = decisionRepository.findBySubmissionIdIn(submissionIds).stream()
        .collect(Collectors.toMap(Decision::getSubmissionId, d -> d));

    // Batch fetch review summaries
    Map<Long, ReviewSummaryDTO> summaryMap = getReviewSummaries(submissionIds);

    // Batch fetch decidedBy users
    List<Long> userIds = decisionMap.values().stream().map(Decision::getDecidedBy).collect(Collectors.toList());
    Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
        .collect(Collectors.toMap(User::getId, u -> u));

    return submissions.stream()
        .map(submission -> {
          Decision decision = decisionMap.get(submission.getId());
          ReviewSummaryDTO reviewSummary = summaryMap.get(submission.getId());

          if (decision != null) {
            User decidedByUser = userMap.get(decision.getDecidedBy());
            return DecisionResultDTO.builder()
                .id(decision.getId())
                .submissionId(decision.getSubmissionId())
                .submissionTitle(submission.getTitle())
                .decidedBy(decision.getDecidedBy())
                .decidedByName(decidedByUser != null ? decidedByUser.getFullName() : null)
                .type(decision.getType().name())
                .comments(decision.getComments())
                .notified(decision.getNotified())
                .locked(decision.getLocked())
                .decidedAt(decision.getDecidedAt())
                .reviewSummary(reviewSummary)
                .build();
          } else {
            return DecisionResultDTO.builder()
                .submissionId(submission.getId())
                .submissionTitle(submission.getTitle())
                .reviewSummary(reviewSummary)
                .build();
          }
        })
        .collect(Collectors.toList());
  }

  // Lấy danh sách decision chưa gửi thông báo
  public List<DecisionResultDTO> getPendingNotifications() {
    return decisionRepository.findByNotifiedFalse().stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  public Decision getDecisionEntityById(Long decisionId) {
    return decisionRepository
        .findById(decisionId)
        .orElseThrow(() -> new NotFoundException("Decision not found"));
  }

  /**
   * Cập nhật decision (chỉ cho phép khi chưa locked/notified)
   *
   * @param decisionId Decision ID
   * @param dto        Update request DTO
   * @param chairId    Chair user ID
   * @return Updated DecisionResultDTO
   */
  @Transactional
  public DecisionResultDTO updateDecision(
      Long decisionId, com.uth.confms.decision.dto.UpdateDecisionRequestDTO dto, Long chairId, boolean isAdmin) {
    Decision decision = decisionRepository
        .findById(decisionId)
        .orElseThrow(() -> new NotFoundException("Decision not found"));

    // Check if decision is locked
    if (decision.getLocked() != null && decision.getLocked()) {
      throw new BusinessException(
          "Cannot update decision: Decision has been locked after notification");
    }

    // Check if already notified
    if (decision.getNotified() != null && decision.getNotified()) {
      throw new BusinessException(
          "Cannot update decision: Notification has already been sent to author");
    }

    // Get submission and conference for authorization check
    Submission submission = submissionRepository
        .findById(decision.getSubmissionId())
        .orElseThrow(() -> new NotFoundException("Submission not found"));

    Conference conference = conferenceRepository
        .findById(submission.getConferenceId())
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization - only chair (or admin) can update decisions
    if (!isAdmin && !conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair or admin can update decisions");
    }

    // Track changes for history
    boolean hasChanges = false;

    // Update type if provided
    if (dto.getType() != null && !dto.getType().isEmpty()) {
      Decision.DecisionType newType;
      try {
        newType = Decision.DecisionType.valueOf(dto.getType());
      } catch (IllegalArgumentException e) {
        throw new BusinessException("Invalid decision type: " + dto.getType());
      }

      if (decision.getType() != newType) {
        String oldType = decision.getType().name();
        decision.setType(newType);
        hasChanges = true;

        // Log type change
        logDecisionChange(
            decisionId,
            chairId,
            DecisionHistory.ChangeType.TYPE_CHANGED,
            oldType,
            newType.name(),
            "type",
            dto.getReason());

        // Update submission status accordingly
        if (newType == Decision.DecisionType.ACCEPT
            || newType == Decision.DecisionType.CONDITIONAL_ACCEPT) {
          submission.setStatus(Submission.SubmissionStatus.ACCEPTED);
        } else {
          submission.setStatus(Submission.SubmissionStatus.REJECTED);
        }
        submissionRepository.save(submission);
      }
    }

    // Update comments if provided
    if (dto.getComments() != null) {
      String oldComments = decision.getComments();
      if (!dto.getComments().equals(oldComments)) {
        decision.setComments(dto.getComments());
        hasChanges = true;

        // Log comments change
        logDecisionChange(
            decisionId,
            chairId,
            DecisionHistory.ChangeType.COMMENTS_CHANGED,
            oldComments,
            dto.getComments(),
            "comments",
            dto.getReason());
      }
    }

    if (!hasChanges) {
      throw new BusinessException("No changes provided");
    }

    decision = decisionRepository.save(decision);
    return mapToDTO(decision);
  }

  /**
   * Lấy review summary cho submission để hỗ trợ decision-making
   *
   * @param submissionId Submission ID
   * @return ReviewSummaryDTO với average score, review count, và score
   *         distribution
   */
  public ReviewSummaryDTO getReviewSummary(Long submissionId) {
    // Get submitted reviews
    List<Review> reviews = reviewRepository.findBySubmissionIdAndStatus(
        submissionId, Review.ReviewStatus.SUBMITTED);

    return calculateSummary(submissionId, reviews);
  }

  // Lấy review summary cho danh sách submission (dùng cho batch processing)
  public Map<Long, ReviewSummaryDTO> getReviewSummaries(List<Long> submissionIds) {
    if (submissionIds == null || submissionIds.isEmpty()) {
      return new HashMap<>();
    }

    List<Review> allReviews = reviewRepository.findBySubmissionIdIn(submissionIds);
    Map<Long, List<Review>> groups = allReviews.stream()
        .filter(r -> r.getStatus() == Review.ReviewStatus.SUBMITTED)
        .collect(Collectors.groupingBy(Review::getSubmissionId));

    Map<Long, ReviewSummaryDTO> results = new HashMap<>();
    for (Long subId : submissionIds) {
      results.put(subId, calculateSummary(subId, groups.getOrDefault(subId, List.of())));
    }
    return results;
  }

  private ReviewSummaryDTO calculateSummary(Long submissionId, List<Review> reviews) {
    if (reviews == null || reviews.isEmpty()) {
      Map<String, Integer> emptyDistribution = new HashMap<>();
      emptyDistribution.put("STRONG_ACCEPT", 0);
      emptyDistribution.put("ACCEPT", 0);
      emptyDistribution.put("WEAK_ACCEPT", 0);
      emptyDistribution.put("BORDERLINE", 0);
      emptyDistribution.put("WEAK_REJECT", 0);
      emptyDistribution.put("REJECT", 0);
      emptyDistribution.put("STRONG_REJECT", 0);
      return new ReviewSummaryDTO(submissionId, null, 0, emptyDistribution);
    }

    // Calculate average score
    double sum = 0.0;
    int count = 0;
    for (Review review : reviews) {
      if (review.getNumericScore() != null) {
        sum += review.getNumericScore();
        count++;
      }
    }
    Double averageScore = count > 0 ? sum / count : null;

    // Calculate score distribution
    Map<String, Integer> scoreDistribution = new HashMap<>();
    scoreDistribution.put("STRONG_ACCEPT", 0);
    scoreDistribution.put("ACCEPT", 0);
    scoreDistribution.put("WEAK_ACCEPT", 0);
    scoreDistribution.put("BORDERLINE", 0);
    scoreDistribution.put("WEAK_REJECT", 0);
    scoreDistribution.put("REJECT", 0);
    scoreDistribution.put("STRONG_REJECT", 0);

    for (Review review : reviews) {
      if (review.getScore() != null) {
        scoreDistribution.merge(review.getScore().name(), 1, Integer::sum);
      }
    }

    return new ReviewSummaryDTO(submissionId, averageScore, reviews.size(), scoreDistribution);
  }

  /**
   * Log decision change vào history table
   *
   * @param decisionId  Decision ID
   * @param changedBy   User ID who made the change
   * @param changeType  Type of change
   * @param oldValue    Old value (if applicable)
   * @param newValue    New value (if applicable)
   * @param fieldName   Field name that changed
   * @param description Description of the change
   */
  private void logDecisionChange(
      Long decisionId,
      Long changedBy,
      DecisionHistory.ChangeType changeType,
      String oldValue,
      String newValue,
      String fieldName,
      String description) {
    DecisionHistory history = DecisionHistory.builder()
        .decisionId(decisionId)
        .changedBy(changedBy)
        .changeType(changeType)
        .oldValue(oldValue)
        .newValue(newValue)
        .fieldName(fieldName)
        .description(description)
        .build();
    decisionHistoryRepository.save(history);
  }

  /**
   * Lấy lịch sử thay đổi của decision
   *
   * @param decisionId Decision ID
   * @return List of DecisionHistory entries
   */
  public List<DecisionHistory> getDecisionHistory(Long decisionId) {
    return decisionHistoryRepository.findByDecisionIdOrderByChangedAtDesc(decisionId);
  }

  private DecisionResultDTO mapToDTO(Decision decision) {
    Submission submission = submissionRepository.findById(decision.getSubmissionId()).orElse(null);
    User decidedByUser = userRepository.findById(decision.getDecidedBy()).orElse(null);

    // Get review summary for decision-making support
    ReviewSummaryDTO reviewSummary = getReviewSummary(decision.getSubmissionId());

    return DecisionResultDTO.builder()
        .id(decision.getId())
        .submissionId(decision.getSubmissionId())
        .submissionTitle(submission != null ? submission.getTitle() : null)
        .decidedBy(decision.getDecidedBy())
        .decidedByName(decidedByUser != null ? decidedByUser.getFullName() : null)
        .type(decision.getType().name())
        .comments(decision.getComments())
        .notified(decision.getNotified())
        .locked(decision.getLocked())
        .decidedAt(decision.getDecidedAt())
        .reviewSummary(reviewSummary)
        .build();
  }
}
