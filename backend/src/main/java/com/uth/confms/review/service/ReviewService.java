package com.uth.confms.review.service;

import com.uth.confms.assignment.entity.Assignment;
import com.uth.confms.assignment.repository.AssignmentRepository;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.entity.Deadline;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.conference.repository.DeadlineRepository;
import com.uth.confms.review.dto.AverageScoreDTO;
import com.uth.confms.review.dto.ReviewerPerformanceDTO;
import com.uth.confms.review.dto.ReviewResponseDTO;
import com.uth.confms.review.dto.ReviewStatisticsDTO;
import com.uth.confms.review.dto.ReviewSubmitDTO;
import com.uth.confms.review.entity.Review;
import com.uth.confms.review.entity.ReviewTemplate;
import com.uth.confms.review.repository.ReviewRepository;
import com.uth.confms.review.repository.ReviewTemplateRepository;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.repository.SubmissionRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý reviews (đánh giá bài nộp)
 *
 * <p>
 * Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 * <li>Tạo và cập nhật draft reviews
 * <li>Submit reviews
 * <li>Double-blind review (ẩn reviewer identity)
 * <li>Quản lý review status và scores
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class ReviewService {
  private final ReviewRepository reviewRepository;
  private final AssignmentRepository assignmentRepository;
  private final SubmissionRepository submissionRepository;
  private final UserRepository userRepository;
  private final ConferenceRepository conferenceRepository;
  private final DeadlineRepository deadlineRepository;
  private final ReviewTemplateRepository templateRepository;

  public ReviewService(
      ReviewRepository reviewRepository,
      AssignmentRepository assignmentRepository,
      SubmissionRepository submissionRepository,
      UserRepository userRepository,
      ConferenceRepository conferenceRepository,
      DeadlineRepository deadlineRepository,
      ReviewTemplateRepository templateRepository) {
    this.reviewRepository = reviewRepository;
    this.assignmentRepository = assignmentRepository;
    this.submissionRepository = submissionRepository;
    this.userRepository = userRepository;
    this.conferenceRepository = conferenceRepository;
    this.deadlineRepository = deadlineRepository;
    this.templateRepository = templateRepository;
  }

  @Transactional
  // Tạo hoặc cập nhật draft review
  public ReviewResponseDTO createOrUpdateDraft(ReviewSubmitDTO dto, Long reviewerId) {
    Assignment assignment = assignmentRepository
        .findById(dto.getAssignmentId())
        .orElseThrow(() -> new NotFoundException("Assignment not found"));

    // Check authorization
    if (!assignment.getReviewerId().equals(reviewerId)) {
      throw new UnauthorizedException("You can only create reviews for your own assignments");
    }

    // Check assignment status
    if (assignment.getStatus() != Assignment.AssignmentStatus.ACCEPTED) {
      throw new BusinessException("Assignment must be accepted before creating review");
    }

    // Check review deadline
    checkReviewDeadline(assignment.getSubmissionId());

    // Apply template if provided (only for new reviews)
    ReviewSubmitDTO dtoToUse = dto;
    Review review = reviewRepository
        .findByAssignmentIdAndReviewerId(dto.getAssignmentId(), reviewerId)
        .orElse(null);

    if (review == null && dto.getTemplateId() != null) {
      // Apply template for new review
      dtoToUse = applyTemplate(dto, assignment.getSubmissionId());
    }

    if (review == null) {
      // Create new draft
      review = Review.builder()
          .assignmentId(dto.getAssignmentId())
          .submissionId(assignment.getSubmissionId())
          .reviewerId(reviewerId)
          .summary(dtoToUse.getSummary())
          .strengths(dtoToUse.getStrengths())
          .weaknesses(dtoToUse.getWeaknesses())
          .comments(dtoToUse.getComments())
          .score(Review.ReviewScore.valueOf(dtoToUse.getScore()))
          .status(Review.ReviewStatus.DRAFT)
          .isConfidential(dtoToUse.getIsConfidential())
          .overallRating(dtoToUse.getOverallRating())
          .confidence(dtoToUse.getConfidence())
          .numericScore(Review.ReviewScore.valueOf(dtoToUse.getScore()).toNumericScore())
          .build();
    } else {
      // Update existing draft (only if still in DRAFT status)
      if (review.getStatus() != Review.ReviewStatus.DRAFT) {
        throw new BusinessException("Cannot update submitted review");
      }

      review.setSummary(dto.getSummary());
      review.setStrengths(dto.getStrengths());
      review.setWeaknesses(dto.getWeaknesses());
      review.setComments(dto.getComments());
      review.setScore(Review.ReviewScore.valueOf(dto.getScore()));
      review.setIsConfidential(dto.getIsConfidential());
      review.setOverallRating(dto.getOverallRating());
      review.setConfidence(dto.getConfidence());
      review.setNumericScore(Review.ReviewScore.valueOf(dto.getScore()).toNumericScore());
    }

    review = reviewRepository.save(review);

    // Get review mode from conference
    boolean showReviewerName = shouldShowReviewerName(review.getSubmissionId(), null, false);
    return mapToDTO(review, showReviewerName);
  }

  @Transactional
  // Submit review (không thể sửa sau khi submit)
  public ReviewResponseDTO submitReview(Long reviewId, Long reviewerId) {
    Review review = reviewRepository
        .findById(reviewId)
        .orElseThrow(() -> new NotFoundException("Review not found"));

    // Check authorization
    if (!review.getReviewerId().equals(reviewerId)) {
      throw new UnauthorizedException("You can only submit your own reviews");
    }

    // Check status
    if (review.getStatus() != Review.ReviewStatus.DRAFT) {
      throw new BusinessException("Review is already submitted");
    }

    // Check review deadline
    checkReviewDeadline(review.getSubmissionId());

    // Validate required fields
    if (review.getSummary() == null || review.getSummary().trim().isEmpty()) {
      throw new BusinessException("Summary is required");
    }
    if (review.getComments() == null || review.getComments().trim().isEmpty()) {
      throw new BusinessException("Comments are required");
    }

    review.setStatus(Review.ReviewStatus.SUBMITTED);
    review.setSubmittedAt(LocalDateTime.now());
    review = reviewRepository.save(review);

    // Update assignment status to COMPLETED
    Assignment assignment = assignmentRepository.findById(review.getAssignmentId()).orElse(null);
    if (assignment != null) {
      assignment.setStatus(Assignment.AssignmentStatus.COMPLETED);
      assignmentRepository.save(assignment);

      // Transition submission to REVIEWED if all current assignments (not DECLINED)
      // are completed
      List<Assignment> allAssignments = assignmentRepository.findBySubmissionId(review.getSubmissionId());

      // Filter out DECLINED assignments and check if others are COMPLETED
      List<Assignment> activeAssignments = allAssignments.stream()
          .filter(a -> a.getStatus() != Assignment.AssignmentStatus.DECLINED)
          .collect(Collectors.toList());

      boolean allCompleted = !activeAssignments.isEmpty() && activeAssignments.stream()
          .allMatch(a -> a.getStatus() == Assignment.AssignmentStatus.COMPLETED);

      if (allCompleted) {
        Submission submission = submissionRepository.findById(review.getSubmissionId()).orElse(null);
        if (submission != null && (submission.getStatus() == Submission.SubmissionStatus.UNDER_REVIEW
            || submission.getStatus() == Submission.SubmissionStatus.SUBMITTED)) {
          submission.setStatus(Submission.SubmissionStatus.REVIEWED);
          submissionRepository.save(submission);
        }
      }
    }

    // Get review mode from conference
    boolean showReviewerName = shouldShowReviewerName(review.getSubmissionId(), null, false);
    return mapToDTO(review, showReviewerName);
  }

  // Lấy review của user hiện tại cho assignment
  public ReviewResponseDTO getMyReview(Long assignmentId, Long reviewerId) {
    Review review = reviewRepository
        .findByAssignmentIdAndReviewerId(assignmentId, reviewerId)
        .orElseThrow(() -> new NotFoundException("Review not found"));

    // Check authorization
    if (!review.getReviewerId().equals(reviewerId)) {
      throw new UnauthorizedException("You can only view your own reviews");
    }

    return mapToDTO(review, true); // true = show reviewer name (own review)
  }

  // Lấy danh sách reviews của submission (author chỉ thấy submitted &
  // non-confidential)
  public List<ReviewResponseDTO> getReviewsBySubmission(
      Long submissionId, Long userId, boolean isChairOrAdmin) {
    // Validate submission exists
    submissionRepository
        .findById(submissionId)
        .orElseThrow(() -> new NotFoundException("Submission not found"));

    List<Review> reviews = reviewRepository.findBySubmissionId(submissionId);

    // Determine if reviewer names should be shown based on review mode
    boolean showReviewerName = shouldShowReviewerName(submissionId, userId, isChairOrAdmin);

    return reviews.stream()
        .filter(review -> {
          // Chair/Admin can see all
          if (isChairOrAdmin)
            return true;
          // Reviewer can see own review (even if draft)
          if (review.getReviewerId().equals(userId))
            return true;

          // For others (Authors):
          // 1. Must be SUBMITTED
          // 2. Must not be confidential
          return review.getStatus() == Review.ReviewStatus.SUBMITTED && !review.getIsConfidential();
        })
        .map(review -> {
          // For own review, always show reviewer name
          boolean showName = review.getReviewerId().equals(userId) || showReviewerName;
          return mapToDTO(review, showName);
        })
        .collect(Collectors.toList());
  }

  // Lấy chi tiết review
  public ReviewResponseDTO getReview(Long reviewId, Long userId, boolean isChairOrAdmin) {
    Review review = reviewRepository
        .findById(reviewId)
        .orElseThrow(() -> new NotFoundException("Review not found"));

    // Check authorization: reviewer can see own review, chair/admin can see all
    boolean isOwner = review.getReviewerId().equals(userId);
    boolean isAuthor = false;
    try {
      var submission = submissionRepository.findById(review.getSubmissionId()).orElse(null);
      if (submission != null && submission.getAuthorId().equals(userId)) {
        isAuthor = true;
      }
    } catch (Exception e) {
      // Ignore navigation/author lookup errors
    }

    boolean canView = isOwner || isChairOrAdmin
        || (isAuthor && review.getStatus() == Review.ReviewStatus.SUBMITTED && !review.getIsConfidential());
    if (!canView) {
      throw new UnauthorizedException("You don't have permission to view this review");
    }

    // Determine if reviewer name should be shown based on review mode
    boolean showReviewerName = shouldShowReviewerName(review.getSubmissionId(), userId, isChairOrAdmin);
    // For own review, always show reviewer name
    if (review.getReviewerId().equals(userId)) {
      showReviewerName = true;
    }

    return mapToDTO(review, showReviewerName);
  }

  /**
   * Determine if reviewer name should be shown based on conference review mode
   *
   * @param submissionId   Submission ID
   * @param userId         Current user ID (null if not authenticated)
   * @param isChairOrAdmin Whether user is chair or admin
   * @return true if reviewer name should be shown
   */
  private boolean shouldShowReviewerName(Long submissionId, Long userId, boolean isChairOrAdmin) {
    try {
      var submission = submissionRepository.findById(submissionId).orElse(null);
      if (submission == null) {
        return false;
      }

      var conference = conferenceRepository.findById(submission.getConferenceId()).orElse(null);
      if (conference == null) {
        return false;
      }

      Conference.ReviewMode reviewMode = conference.getReviewMode();

      // Chair/admin can always see reviewer name
      if (isChairOrAdmin) {
        return true;
      }

      // Single-blind: reviewer knows author, author can see reviewer name
      // Double-blind: neither knows the other
      if (reviewMode == Conference.ReviewMode.SINGLE_BLIND) {
        // In single-blind, author can see reviewer name
        if (userId != null && submission != null && submission.getAuthorId().equals(userId)) {
          return true; // Author can see reviewer name in single-blind mode
        }
      }
      // Double-blind: only chair/admin can see reviewer name (already checked above)
      return false;
    } catch (Exception e) {
      // If error, default to double-blind behavior (don't show reviewer name)
      return isChairOrAdmin;
    }
  }

  /**
   * Check if review deadline has passed
   *
   * @param submissionId Submission ID
   * @throws BusinessException If deadline has passed and is hard deadline
   */
  // Kiểm tra deadline review
  private void checkReviewDeadline(Long submissionId) {
    Submission submission = submissionRepository
        .findById(submissionId)
        .orElseThrow(() -> new NotFoundException("Submission not found"));

    List<Deadline> deadlines = deadlineRepository.findByConferenceId(submission.getConferenceId());
    Deadline reviewDeadline = deadlines.stream()
        .filter(d -> d.getType() == Deadline.DeadlineType.REVIEW)
        .findFirst()
        .orElse(null);

    if (reviewDeadline != null && reviewDeadline.getDueDate().isBefore(LocalDateTime.now())) {
      if (reviewDeadline.getHardDeadline()) {
        throw new BusinessException("Review deadline has passed");
      }
    }
  }

  private ReviewResponseDTO mapToDTO(Review review, boolean showReviewerName) {
    User reviewer = showReviewerName ? userRepository.findById(review.getReviewerId()).orElse(null) : null;

    return ReviewResponseDTO.builder()
        .id(review.getId())
        .assignmentId(review.getAssignmentId())
        .submissionId(review.getSubmissionId())
        .reviewerId(review.getReviewerId())
        .reviewerName(reviewer != null ? reviewer.getFullName() : null)
        .summary(review.getSummary())
        .strengths(review.getStrengths())
        .weaknesses(review.getWeaknesses())
        .comments(review.getComments())
        .score(review.getScore().name())
        .status(review.getStatus().name())
        .isConfidential(review.getIsConfidential())
        .overallRating(review.getOverallRating())
        .confidence(review.getConfidence())
        .numericScore(review.getNumericScore())
        .createdAt(review.getCreatedAt())
        .submittedAt(review.getSubmittedAt())
        .build();
  }

  /**
   * Tính average numeric score cho submission
   *
   * @param submissionId Submission ID
   * @return AverageScoreDTO với average score và review count
   */
  // Tính điểm trung bình của submission
  public AverageScoreDTO getAverageScore(Long submissionId) {
    // Validate submission exists
    submissionRepository
        .findById(submissionId)
        .orElseThrow(() -> new NotFoundException("Submission not found"));

    List<Review> reviews = reviewRepository.findBySubmissionIdAndStatus(
        submissionId, Review.ReviewStatus.SUBMITTED);

    if (reviews.isEmpty()) {
      return new AverageScoreDTO(submissionId, null, 0);
    }

    double sum = 0.0;
    int count = 0;
    for (Review review : reviews) {
      if (review.getNumericScore() != null) {
        sum += review.getNumericScore();
        count++;
      }
    }

    Double averageScore = count > 0 ? sum / count : null;
    return new AverageScoreDTO(submissionId, averageScore, count);
  }

  /**
   * Tính review statistics cho conference
   *
   * @param conferenceId Conference ID
   * @param chairId      Chair ID để check authorization
   * @return ReviewStatisticsDTO với các metrics
   */
  // Lấy thống kê review cho conference
  public ReviewStatisticsDTO getReviewStatistics(Long conferenceId, Long chairId) {
    // Check authorization
    Conference conference = conferenceRepository
        .findById(conferenceId)
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can view review statistics");
    }

    // Get all submissions for this conference
    List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
    List<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());

    // Optimized: Use findBySubmissionIdIn to get all reviews in one query
    List<Review> allReviews = reviewRepository.findBySubmissionIdIn(submissionIds);

    // Calculate total reviews
    int totalReviews = allReviews.size();
    int completedReviews = (int) allReviews.stream()
        .filter(r -> r.getStatus() == Review.ReviewStatus.SUBMITTED)
        .count();
    int pendingReviews = (int) allReviews.stream()
        .filter(r -> r.getStatus() == Review.ReviewStatus.DRAFT)
        .count();

    // Calculate completion rate
    double completionRate = totalReviews > 0 ? (double) completedReviews / totalReviews * 100.0 : 0.0;

    // Calculate average score
    List<Review> submittedReviews = allReviews.stream()
        .filter(r -> r.getStatus() == Review.ReviewStatus.SUBMITTED)
        .collect(Collectors.toList());

    double averageScore = 0.0;
    if (!submittedReviews.isEmpty()) {
      double sum = 0.0;
      int count = 0;
      for (Review review : submittedReviews) {
        if (review.getNumericScore() != null) {
          sum += review.getNumericScore();
          count++;
        }
      }
      averageScore = count > 0 ? sum / count : 0.0;
    }

    // Score distribution
    Map<String, Integer> scoreDistribution = new HashMap<>();
    scoreDistribution.put("STRONG_ACCEPT", 0);
    scoreDistribution.put("ACCEPT", 0);
    scoreDistribution.put("WEAK_ACCEPT", 0);
    scoreDistribution.put("BORDERLINE", 0);
    scoreDistribution.put("WEAK_REJECT", 0);
    scoreDistribution.put("REJECT", 0);
    scoreDistribution.put("STRONG_REJECT", 0);

    for (Review review : submittedReviews) {
      if (review.getScore() != null) {
        scoreDistribution.merge(review.getScore().name(), 1, Integer::sum);
      }
    }

    // Average completion time
    double averageCompletionTime = 0.0;
    List<Long> completionTimes = new ArrayList<>();
    for (Review review : submittedReviews) {
      if (review.getSubmittedAt() != null && review.getCreatedAt() != null) {
        long days = ChronoUnit.DAYS.between(review.getCreatedAt(), review.getSubmittedAt());
        completionTimes.add(days);
      }
    }
    if (!completionTimes.isEmpty()) {
      averageCompletionTime = completionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }

    // Submission timeline (reviews submitted per day)
    Map<LocalDateTime, Integer> submissionTimeline = new HashMap<>();
    for (Review review : submittedReviews) {
      if (review.getSubmittedAt() != null) {
        LocalDateTime day = review.getSubmittedAt().toLocalDate().atStartOfDay();
        submissionTimeline.merge(day, 1, Integer::sum);
      }
    }

    // Reviewer performance metrics
    Map<Long, ReviewerPerformanceDTO> reviewerMetrics = new HashMap<>();
    Map<Long, List<Review>> reviewsByReviewer = allReviews.stream()
        .collect(Collectors.groupingBy(Review::getReviewerId));

    // Batch fetch reviewer details
    List<Long> allReviewerIds = new ArrayList<>(reviewsByReviewer.keySet());
    Map<Long, User> userMap = userRepository.findAllById(allReviewerIds).stream()
        .collect(Collectors.toMap(User::getId, u -> u));

    for (Map.Entry<Long, List<Review>> entry : reviewsByReviewer.entrySet()) {
      Long reviewerId = entry.getKey();
      List<Review> reviewerReviews = entry.getValue();

      int reviewerTotalReviews = reviewerReviews.size();
      int reviewerCompletedReviews = (int) reviewerReviews.stream()
          .filter(r -> r.getStatus() == Review.ReviewStatus.SUBMITTED)
          .count();

      double reviewerCompletionRate = reviewerTotalReviews > 0
          ? (double) reviewerCompletedReviews / reviewerTotalReviews * 100.0
          : 0.0;

      // Average score for this reviewer
      List<Review> reviewerSubmittedReviews = reviewerReviews.stream()
          .filter(r -> r.getStatus() == Review.ReviewStatus.SUBMITTED)
          .collect(Collectors.toList());

      double reviewerAverageScore = 0.0;
      if (!reviewerSubmittedReviews.isEmpty()) {
        double sum = 0.0;
        int count = 0;
        for (Review review : reviewerSubmittedReviews) {
          if (review.getNumericScore() != null) {
            sum += review.getNumericScore();
            count++;
          }
        }
        reviewerAverageScore = count > 0 ? sum / count : 0.0;
      }

      // Average completion time for this reviewer
      double reviewerAverageCompletionTime = 0.0;
      List<Long> reviewerCompletionTimes = new ArrayList<>();
      for (Review review : reviewerSubmittedReviews) {
        if (review.getSubmittedAt() != null && review.getCreatedAt() != null) {
          long days = ChronoUnit.DAYS.between(review.getCreatedAt(), review.getSubmittedAt());
          reviewerCompletionTimes.add(days);
        }
      }
      if (!reviewerCompletionTimes.isEmpty()) {
        reviewerAverageCompletionTime = reviewerCompletionTimes.stream().mapToLong(Long::longValue).average()
            .orElse(0.0);
      }

      User reviewer = userMap.get(reviewerId);
      String reviewerName = reviewer != null ? reviewer.getFullName() : "Unknown";

      reviewerMetrics.put(
          reviewerId,
          new ReviewerPerformanceDTO(
              reviewerId,
              reviewerName,
              reviewerTotalReviews,
              reviewerCompletedReviews,
              reviewerAverageScore,
              reviewerAverageCompletionTime,
              reviewerCompletionRate));
    }

    return new ReviewStatisticsDTO(
        conferenceId,
        completionRate,
        averageScore,
        scoreDistribution,
        averageCompletionTime,
        totalReviews,
        completedReviews,
        pendingReviews,
        submissionTimeline,
        reviewerMetrics);
  }

  /**
   * Apply review template to DTO
   *
   * @param dto          Original DTO
   * @param submissionId Submission ID to get conference
   * @return DTO with template fields applied (if template exists)
   */
  private ReviewSubmitDTO applyTemplate(ReviewSubmitDTO dto, Long submissionId) {
    if (dto.getTemplateId() == null) {
      return dto; // No template to apply
    }

    Submission submission = submissionRepository
        .findById(submissionId)
        .orElseThrow(() -> new NotFoundException("Submission not found"));

    Long conferenceId = submission.getConferenceId();

    // Try to find template (conference-specific first, then global)
    ReviewTemplate template = templateRepository
        .findById(dto.getTemplateId())
        .orElseThrow(() -> new NotFoundException("Review template not found"));

    // Check if template is for this conference or is global
    if (template.getConferenceId() != null && !template.getConferenceId().equals(conferenceId)) {
      throw new BusinessException("Template is not available for this conference");
    }

    // Create new DTO with template fields applied (only if DTO field is empty)
    ReviewSubmitDTO result = new ReviewSubmitDTO();
    result.setAssignmentId(dto.getAssignmentId());
    result.setSummary(
        dto.getSummary() != null && !dto.getSummary().trim().isEmpty()
            ? dto.getSummary()
            : template.getSummary());
    result.setStrengths(
        dto.getStrengths() != null && !dto.getStrengths().trim().isEmpty()
            ? dto.getStrengths()
            : template.getStrengths());
    result.setWeaknesses(
        dto.getWeaknesses() != null && !dto.getWeaknesses().trim().isEmpty()
            ? dto.getWeaknesses()
            : template.getWeaknesses());
    result.setComments(
        dto.getComments() != null && !dto.getComments().trim().isEmpty()
            ? dto.getComments()
            : template.getComments());
    result.setScore(
        dto.getScore() != null && !dto.getScore().trim().isEmpty()
            ? dto.getScore()
            : template.getDefaultScore().name());
    result.setIsConfidential(dto.getIsConfidential());
    result.setOverallRating(dto.getOverallRating());
    result.setConfidence(dto.getConfidence());

    return result;
  }
}
