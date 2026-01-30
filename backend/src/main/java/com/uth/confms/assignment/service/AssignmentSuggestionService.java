package com.uth.confms.assignment.service;

import com.uth.confms.assignment.dto.AssignmentSuggestionDTO;
import com.uth.confms.assignment.entity.Assignment;
import com.uth.confms.assignment.repository.AssignmentRepository;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.entity.Topic;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.conference.repository.TopicRepository;
import com.uth.confms.pc.entity.PCMember;
import com.uth.confms.pc.repository.PCMemberRepository;
import com.uth.confms.pc.service.COIService;
import com.uth.confms.review.entity.Review;
import com.uth.confms.review.repository.ReviewRepository;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.entity.SubmissionAuthor;
import com.uth.confms.submission.repository.SubmissionAuthorRepository;
import com.uth.confms.submission.repository.SubmissionRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

/**
 * Service cung cấp AI suggestions cho reviewer assignments
 *
 * <p>
 * Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 * <li>Generate AI suggestions cho reviewers (không auto-assign)
 * <li>Tính toán suggestion scores dựa trên workload và fit
 * <li>Loại trừ reviewers có COI hoặc là authors
 * <li>Sort suggestions theo score
 * </ul>
 *
 * <p>
 * <b>Lưu ý:</b> Service này chỉ suggest, không tự động assign reviewers.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class AssignmentSuggestionService {
  private final PCMemberRepository pcMemberRepository;
  private final SubmissionRepository submissionRepository;
  private final SubmissionAuthorRepository submissionAuthorRepository;
  private final AssignmentRepository assignmentRepository;
  private final UserRepository userRepository;
  private final COIService coiService;
  private final ConferenceRepository conferenceRepository;
  @SuppressWarnings("unused")
  private final TopicRepository topicRepository; // Reserved for future use
  private final ReviewRepository reviewRepository;

  public AssignmentSuggestionService(
      PCMemberRepository pcMemberRepository,
      SubmissionRepository submissionRepository,
      SubmissionAuthorRepository submissionAuthorRepository,
      AssignmentRepository assignmentRepository,
      UserRepository userRepository,
      COIService coiService,
      ConferenceRepository conferenceRepository,
      TopicRepository topicRepository,
      ReviewRepository reviewRepository) {
    this.pcMemberRepository = pcMemberRepository;
    this.submissionRepository = submissionRepository;
    this.submissionAuthorRepository = submissionAuthorRepository;
    this.assignmentRepository = assignmentRepository;
    this.userRepository = userRepository;
    this.coiService = coiService;
    this.conferenceRepository = conferenceRepository;
    this.topicRepository = topicRepository;
    this.reviewRepository = reviewRepository;
  }

  /**
   * Lấy AI suggestions cho reviewers cho một submission
   *
   * <p>
   * Service này chỉ suggest, không tự động assign reviewers. Suggestions được
   * tính toán dựa
   * trên:
   *
   * <ul>
   * <li>Workload của reviewer (số assignments hiện tại)
   * <li>COI status
   * <li>Author exclusion
   * <li>Target: 3 reviewers per submission
   * </ul>
   *
   * @param submissionId ID của submission cần suggest reviewers
   * @return Danh sách suggested reviewers với scores và reasons, sorted by score
   *         descending
   */
  public List<AssignmentSuggestionDTO> getSuggestions(Long submissionId) {
    Submission submission = submissionRepository
        .findById(submissionId)
        .orElseThrow(
            () -> new NotFoundException("Submission with id " + submissionId + " not found"));

    // Detect institutional COI before generating suggestions
    try {
      coiService.detectInstitutionalConflicts(submissionId);
    } catch (Exception e) {
      // Log and continue
      System.err.println("Failed to detect institutional COI for suggestions: " + e.getMessage());
    }

    // Get all PC members for this conference (with expertiseTopics loaded)
    List<PCMember> pcMembers = pcMemberRepository.findByConferenceIdAndStatusWithExpertise(
        submission.getConferenceId(), PCMember.PCMemberStatus.ACCEPTED);

    // Get submission authors
    List<SubmissionAuthor> authors = submissionAuthorRepository.findBySubmissionId(submissionId);
    List<Long> authorIds = authors.stream().map(SubmissionAuthor::getUserId).toList();

    // Get existing assignments
    List<Assignment> existingAssignments = assignmentRepository.findBySubmissionId(submissionId);
    List<Long> assignedReviewerIds = existingAssignments.stream().map(Assignment::getReviewerId).toList();

    List<AssignmentSuggestionDTO> suggestions = new ArrayList<>();

    for (PCMember pcMember : pcMembers) {
      Long reviewerId = pcMember.getUserId();

      // Skip if already assigned, is an author, or has COI
      if (assignedReviewerIds.contains(reviewerId)
          || authorIds.contains(reviewerId)
          || coiService.hasCOI(reviewerId, submissionId)) {
        continue;
      }

      // Calculate suggestion score (includes workload, keyword/topic matching)
      double score = calculateSuggestionScore(
          pcMember, submission, existingAssignments.size());

      // Only suggest if score > 0
      if (score > 0) {
        User reviewer = userRepository.findById(reviewerId).orElse(null);
        if (reviewer != null) {
          // Generate detailed reason with keyword/topic match info
          String reason = generateSuggestionReason(score, false, pcMember, submission);
          suggestions.add(
              AssignmentSuggestionDTO.builder()
                  .reviewerId(reviewerId)
                  .reviewerEmail(reviewer.getEmail())
                  .reviewerName(reviewer.getFullName())
                  .score(score)
                  .reason(reason)
                  .hasCOI(false)
                  .build());
        }
      }
    }

    // Sort by score descending
    suggestions.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

    return suggestions;
  }

  /**
   * Tính toán suggestion score cho một reviewer
   *
   * <p>
   * Algorithm bao gồm:
   *
   * <ul>
   * <li>Base score: 0.5
   * <li>Workload adjustment: reviewers với ít assignments hơn có score cao hơn
   * <li>Keyword matching: match submission keywords với reviewer expertise
   * keywords
   * <li>Topic matching: match submission topics với reviewer expertise topics
   * <li>Target adjustment: nếu submission chưa đủ 3 reviewers, tăng score
   * </ul>
   *
   * @param pcMember               PC member (reviewer)
   * @param submission             Submission cần assign reviewer
   * @param currentAssignmentCount Số assignments hiện tại của submission này
   * @return Suggestion score từ 0.0 đến 1.0
   */
  private double calculateSuggestionScore(
      PCMember pcMember, Submission submission, int currentAssignmentCount) {
    Long reviewerId = pcMember.getUserId();

    // Base score
    double score = 0.5;

    // Adjust based on current workload (prefer reviewers with fewer assignments)
    long reviewerAssignmentCount = assignmentRepository.countByReviewerIdAndStatus(
        reviewerId, Assignment.AssignmentStatus.ACCEPTED);

    // Lower workload = higher score
    if (reviewerAssignmentCount == 0) {
      score += 0.3;
    } else if (reviewerAssignmentCount < 3) {
      score += 0.2;
    } else if (reviewerAssignmentCount < 5) {
      score += 0.1;
    } else {
      score -= 0.1;
    }

    // Keyword matching: match submission keywords với reviewer expertise keywords
    double keywordMatchScore = calculateKeywordMatchScore(pcMember, submission);
    score += keywordMatchScore * 0.15; // Weight: 15%

    // Topic matching: match submission topics với reviewer expertise topics
    double topicMatchScore = calculateTopicMatchScore(pcMember, submission);
    score += topicMatchScore * 0.15; // Weight: 15%

    // Track-based matching: match submission track với reviewer's historical track
    // experience
    double trackMatchScore = calculateTrackMatchScore(reviewerId, submission);
    score += trackMatchScore * 0.1; // Weight: 10%

    // Historical review quality: prefer reviewers with good review history
    double reviewQualityScore = calculateReviewQualityScore(reviewerId);
    score += reviewQualityScore * 0.1; // Weight: 10%

    // Ensure we have enough reviewers (target: 3 reviewers per submission)
    if (currentAssignmentCount < 3) {
      score += 0.2;
    }

    // Normalize to 0.0 - 1.0
    return Math.max(0.0, Math.min(1.0, score));
  }

  /**
   * Tính toán điểm khớp từ khóa (Keyword match score).
   * So sánh từ khóa của bài báo với từ khóa chuyên môn của reviewer.
   */
  private double calculateKeywordMatchScore(PCMember pcMember, Submission submission) {
    if (submission.getKeywords() == null || submission.getKeywords().trim().isEmpty()) {
      return 0.0; // No keywords to match
    }

    if (pcMember.getExpertiseKeywords() == null
        || pcMember.getExpertiseKeywords().trim().isEmpty()) {
      return 0.0; // Reviewer has no expertise keywords
    }

    // Parse keywords (comma-separated, case-insensitive)
    Set<String> submissionKeywords = Arrays.stream(submission.getKeywords().split(","))
        .map(String::trim)
        .map(String::toLowerCase)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toSet());

    Set<String> reviewerKeywords = Arrays.stream(pcMember.getExpertiseKeywords().split(","))
        .map(String::trim)
        .map(String::toLowerCase)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toSet());

    if (submissionKeywords.isEmpty() || reviewerKeywords.isEmpty()) {
      return 0.0;
    }

    // Calculate match ratio
    Set<String> matches = new HashSet<>(submissionKeywords);
    matches.retainAll(reviewerKeywords);

    if (matches.isEmpty()) {
      return 0.0;
    }

    // Return match ratio (0.0 to 1.0)
    return (double) matches.size() / Math.max(submissionKeywords.size(), reviewerKeywords.size());
  }

  /**
   * Tính toán điểm khớp chủ đề (Topic match score).
   * So sánh các chủ đề của hội nghị mà bài báo thuộc về với chủ đề chuyên môn của
   * reviewer.
   */
  private double calculateTopicMatchScore(PCMember pcMember, Submission submission) {
    // Get conference topics
    Conference conference = conferenceRepository
        .findById(submission.getConferenceId())
        .orElse(null);

    if (conference == null || conference.getTopics().isEmpty()) {
      return 0.0; // No topics to match
    }

    if (pcMember.getExpertiseTopics() == null || pcMember.getExpertiseTopics().isEmpty()) {
      return 0.0; // Reviewer has no expertise topics
    }

    // Get topic IDs
    Set<Long> conferenceTopicIds = conference.getTopics().stream().map(Topic::getId).collect(Collectors.toSet());

    Set<Long> reviewerTopicIds = pcMember.getExpertiseTopics().stream()
        .map(Topic::getId)
        .collect(Collectors.toSet());

    if (conferenceTopicIds.isEmpty() || reviewerTopicIds.isEmpty()) {
      return 0.0;
    }

    // Calculate match ratio
    Set<Long> matches = new HashSet<>(conferenceTopicIds);
    matches.retainAll(reviewerTopicIds);

    if (matches.isEmpty()) {
      return 0.0;
    }

    // Return match ratio (0.0 to 1.0)
    return (double) matches.size()
        / Math.max(conferenceTopicIds.size(), reviewerTopicIds.size());
  }

  /**
   * Tính toán track match score dựa trên reviewer's historical track experience
   *
   * @param reviewerId ID của reviewer
   * @param submission Submission
   * @return Track match score từ 0.0 đến 1.0
   */
  private double calculateTrackMatchScore(Long reviewerId, Submission submission) {
    if (submission.getTrackId() == null) {
      return 0.0; // No track to match
    }

    // Get all submissions that reviewer has reviewed (completed assignments)
    List<Assignment> completedAssignments = assignmentRepository.findByReviewerIdAndStatus(
        reviewerId, Assignment.AssignmentStatus.COMPLETED);

    if (completedAssignments.isEmpty()) {
      return 0.0; // No historical track experience
    }

    // Get track IDs of submissions reviewer has reviewed
    List<Long> reviewedSubmissionIds = completedAssignments.stream().map(Assignment::getSubmissionId)
        .collect(Collectors.toList());
    List<Submission> reviewedSubmissions = submissionRepository.findAllById(reviewedSubmissionIds);

    // Count how many reviews were for the same track
    long sameTrackCount = reviewedSubmissions.stream()
        .filter(s -> submission.getTrackId().equals(s.getTrackId()))
        .count();

    if (sameTrackCount == 0) {
      return 0.0;
    }

    // Return ratio of same-track reviews (0.0 to 1.0)
    // Higher ratio = more experience with this track
    return Math.min(1.0, (double) sameTrackCount / reviewedSubmissions.size());
  }

  /**
   * Tính toán review quality score dựa trên historical review scores
   *
   * @param reviewerId ID của reviewer
   * @return Review quality score từ 0.0 đến 1.0
   */
  private double calculateReviewQualityScore(Long reviewerId) {
    // Get all submitted reviews by this reviewer
    List<Review> reviews = reviewRepository.findByReviewerId(reviewerId).stream()
        .filter(r -> r.getStatus() == Review.ReviewStatus.SUBMITTED)
        .collect(Collectors.toList());

    if (reviews.isEmpty()) {
      return 0.5; // Neutral score if no review history
    }

    // Calculate average review score (STRONG_ACCEPT = 7, STRONG_REJECT = 0)
    double totalScore = 0.0;
    for (Review review : reviews) {
      totalScore += getReviewScoreValue(review.getScore());
    }

    double averageScore = totalScore / reviews.size();

    // Normalize to 0.0 - 1.0 (7.0 max score -> 1.0)
    return averageScore / 7.0;
  }

  /**
   * Convert ReviewScore enum to numeric value
   *
   * @param score ReviewScore enum
   * @return Numeric value (0.0 to 7.0)
   */
  private double getReviewScoreValue(Review.ReviewScore score) {
    if (score == null) {
      return 3.5; // Neutral (BORDERLINE)
    }

    switch (score) {
      case STRONG_ACCEPT:
        return 7.0;
      case ACCEPT:
        return 6.0;
      case WEAK_ACCEPT:
        return 5.0;
      case BORDERLINE:
        return 3.5;
      case WEAK_REJECT:
        return 2.0;
      case REJECT:
        return 1.0;
      case STRONG_REJECT:
        return 0.0;
      default:
        return 3.5; // Neutral
    }
  }

  /**
   * Tạo lý do suggestion dễ hiểu cho người dùng với explanation chi tiết
   *
   * @param score      Suggestion score (0.0 - 1.0)
   * @param hasCOI     Reviewer có COI không
   * @param pcMember   PC member (reviewer)
   * @param submission Submission
   * @return Lý do suggestion với keyword/topic/track/review quality info
   */
  private String generateSuggestionReason(
      double score, boolean hasCOI, PCMember pcMember, Submission submission) {
    if (hasCOI) {
      return "Has conflict of interest";
    }

    List<String> reasons = new ArrayList<>();

    // Add keyword match info
    double keywordMatch = calculateKeywordMatchScore(pcMember, submission);
    if (keywordMatch > 0.5) {
      reasons.add("strong keyword match (" + String.format("%.0f%%", keywordMatch * 100) + ")");
    } else if (keywordMatch > 0.2) {
      reasons.add("partial keyword match (" + String.format("%.0f%%", keywordMatch * 100) + ")");
    }

    // Add topic match info
    double topicMatch = calculateTopicMatchScore(pcMember, submission);
    if (topicMatch > 0.5) {
      reasons.add("strong topic match (" + String.format("%.0f%%", topicMatch * 100) + ")");
    } else if (topicMatch > 0.2) {
      reasons.add("partial topic match (" + String.format("%.0f%%", topicMatch * 100) + ")");
    }

    // Add track match info
    double trackMatch = calculateTrackMatchScore(pcMember.getUserId(), submission);
    if (trackMatch > 0.3) {
      reasons.add("track experience (" + String.format("%.0f%%", trackMatch * 100) + ")");
    }

    // Add review quality info
    double reviewQuality = calculateReviewQualityScore(pcMember.getUserId());
    if (reviewQuality > 0.7) {
      reasons.add("excellent review history");
    } else if (reviewQuality > 0.5) {
      reasons.add("good review history");
    } else if (reviewQuality < 0.3) {
      reasons.add("limited review history");
    }

    // Add workload info
    long reviewerAssignmentCount = assignmentRepository.countByReviewerIdAndStatus(
        pcMember.getUserId(), Assignment.AssignmentStatus.ACCEPTED);
    if (reviewerAssignmentCount == 0) {
      reasons.add("no current assignments");
    } else if (reviewerAssignmentCount < 3) {
      reasons.add("low workload (" + reviewerAssignmentCount + " assignments)");
    } else if (reviewerAssignmentCount >= 5) {
      reasons.add("high workload (" + reviewerAssignmentCount + " assignments)");
    }

    // Build reason string
    String baseReason;
    if (score >= 0.8) {
      baseReason = "Excellent match";
    } else if (score >= 0.6) {
      baseReason = "Good match";
    } else if (score >= 0.4) {
      baseReason = "Fair match";
    } else {
      baseReason = "Low priority";
    }

    if (reasons.isEmpty()) {
      return baseReason + " - suitable reviewer";
    } else {
      return baseReason + " - " + String.join(", ", reasons);
    }
  }
}
