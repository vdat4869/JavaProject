package com.uth.confms.assignment.service;

import com.uth.confms.assignment.dto.AssignmentSuggestionDTO;
import com.uth.confms.assignment.entity.Assignment;
import com.uth.confms.assignment.repository.AssignmentRepository;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.pc.entity.PCMember;
import com.uth.confms.pc.repository.PCMemberRepository;
import com.uth.confms.pc.service.COIService;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.entity.SubmissionAuthor;
import com.uth.confms.submission.repository.SubmissionAuthorRepository;
import com.uth.confms.submission.repository.SubmissionRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Service cung cấp AI suggestions cho reviewer assignments
 *
 * <p>Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 *   <li>Generate AI suggestions cho reviewers (không auto-assign)
 *   <li>Tính toán suggestion scores dựa trên workload và fit
 *   <li>Loại trừ reviewers có COI hoặc là authors
 *   <li>Sort suggestions theo score
 * </ul>
 *
 * <p><b>Lưu ý:</b> Service này chỉ suggest, không tự động assign reviewers.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class AssignmentSuggestionService {
  private final PCMemberRepository pcMemberRepository;
  private final SubmissionRepository submissionRepository;
  private final SubmissionAuthorRepository submissionAuthorRepository;
  private final AssignmentRepository assignmentRepository;
  private final UserRepository userRepository;
  private final COIService coiService;

  public AssignmentSuggestionService(
      PCMemberRepository pcMemberRepository,
      SubmissionRepository submissionRepository,
      SubmissionAuthorRepository submissionAuthorRepository,
      AssignmentRepository assignmentRepository,
      UserRepository userRepository,
      COIService coiService) {
    this.pcMemberRepository = pcMemberRepository;
    this.submissionRepository = submissionRepository;
    this.submissionAuthorRepository = submissionAuthorRepository;
    this.assignmentRepository = assignmentRepository;
    this.userRepository = userRepository;
    this.coiService = coiService;
  }

  /**
   * Lấy AI suggestions cho reviewers cho một submission
   *
   * <p>Service này chỉ suggest, không tự động assign reviewers. Suggestions được tính toán dựa
   * trên:
   *
   * <ul>
   *   <li>Workload của reviewer (số assignments hiện tại)
   *   <li>COI status
   *   <li>Author exclusion
   *   <li>Target: 3 reviewers per submission
   * </ul>
   *
   * @param submissionId ID của submission cần suggest reviewers
   * @return Danh sách suggested reviewers với scores và reasons, sorted by score descending
   */
  public List<AssignmentSuggestionDTO> getSuggestions(Long submissionId) {
    Submission submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(
                () -> new NotFoundException("Submission with id " + submissionId + " not found"));

    // Get all PC members for this conference
    List<PCMember> pcMembers =
        pcMemberRepository.findByConferenceIdAndStatus(
            submission.getConferenceId(), PCMember.PCMemberStatus.ACCEPTED);

    // Get submission authors
    List<SubmissionAuthor> authors = submissionAuthorRepository.findBySubmissionId(submissionId);
    List<Long> authorIds = authors.stream().map(SubmissionAuthor::getUserId).toList();

    // Get existing assignments
    List<Assignment> existingAssignments = assignmentRepository.findBySubmissionId(submissionId);
    List<Long> assignedReviewerIds =
        existingAssignments.stream().map(Assignment::getReviewerId).toList();

    List<AssignmentSuggestionDTO> suggestions = new ArrayList<>();

    for (PCMember pcMember : pcMembers) {
      Long reviewerId = pcMember.getUserId();

      // Skip if already assigned, is an author, or has COI
      if (assignedReviewerIds.contains(reviewerId)
          || authorIds.contains(reviewerId)
          || coiService.hasCOI(reviewerId, submissionId)) {
        continue;
      }

      // Calculate suggestion score (simple algorithm - can be enhanced with AI)
      double score =
          calculateSuggestionScore(reviewerId, existingAssignments.size());

      // Only suggest if score > 0
      if (score > 0) {
        User reviewer = userRepository.findById(reviewerId).orElse(null);
        if (reviewer != null) {
          suggestions.add(
              AssignmentSuggestionDTO.builder()
                  .reviewerId(reviewerId)
                  .reviewerEmail(reviewer.getEmail())
                  .reviewerName(reviewer.getFullName())
                  .score(score)
                  .reason(generateSuggestionReason(score, false))
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
   * <p>Algorithm đơn giản - có thể thay thế bằng AI/ML model trong tương lai. Score được tính dựa
   * trên:
   *
   * <ul>
   *   <li>Base score: 0.5
   *   <li>Workload adjustment: reviewers với ít assignments hơn có score cao hơn
   *   <li>Target adjustment: nếu submission chưa đủ 3 reviewers, tăng score
   * </ul>
   *
   * @param reviewerId ID của reviewer
   * @param currentAssignmentCount Số assignments hiện tại của submission này
   * @return Suggestion score từ 0.0 đến 1.0
   */
  private double calculateSuggestionScore(Long reviewerId, int currentAssignmentCount) {

    // Base score
    double score = 0.5;

    // Adjust based on current workload (prefer reviewers with fewer assignments)
    long reviewerAssignmentCount =
        assignmentRepository.countByReviewerIdAndStatus(
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

    // Ensure we have enough reviewers (target: 3 reviewers per submission)
    if (currentAssignmentCount < 3) {
      score += 0.2;
    }

    // Normalize to 0.0 - 1.0
    return Math.max(0.0, Math.min(1.0, score));
  }

  /**
   * Tạo lý do suggestion dễ hiểu cho người dùng
   *
   * @param score Suggestion score (0.0 - 1.0)
   * @param hasCOI Reviewer có COI không
   * @return Lý do suggestion bằng tiếng Việt
   */
  private String generateSuggestionReason(double score, boolean hasCOI) {
    if (hasCOI) {
      return "Has conflict of interest";
    }

    if (score >= 0.8) {
      return "Excellent match - low workload, good fit";
    } else if (score >= 0.6) {
      return "Good match - suitable reviewer";
    } else if (score >= 0.4) {
      return "Fair match - consider if needed";
    } else {
      return "Low priority suggestion";
    }
  }
}
