package com.uth.confms.review.controller;

import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.enums.RoleName;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.dto.ApiResponse;
import com.uth.confms.review.dto.AverageScoreDTO;
import com.uth.confms.review.dto.RebuttalDTO;
import com.uth.confms.review.dto.RebuttalSubmitDTO;
import com.uth.confms.review.dto.ReviewCommentDTO;
import com.uth.confms.review.dto.ReviewResponseDTO;
import com.uth.confms.review.dto.ReviewStatisticsDTO;
import com.uth.confms.review.dto.ReviewSubmitDTO;
import com.uth.confms.review.service.DiscussionService;
import com.uth.confms.review.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller quản lý reviews và discussions
 *
 * <p>
 * Các endpoints:
 *
 * <ul>
 * <li>POST /api/reviews/draft - Tạo/cập nhật draft review (PC)
 * <li>POST /api/reviews/{id}/submit - Submit review (PC)
 * <li>GET /api/reviews/assignment/{id} - Lấy review của assignment
 * (PC)
 * <li>GET /api/reviews/submission/{id} - Lấy reviews của submission
 * (authenticated)
 * <li>GET /api/reviews/{id} - Lấy review by ID (authenticated)
 * <li>GET /api/reviews/submission/{id}/average-score - Lấy average score
 * (authenticated)
 * <li>GET /api/reviews/conference/{id}/statistics - Lấy review statistics
 * (CHAIR/ADMIN)
 * <li>POST /api/reviews/submission/{id}/comments - Thêm internal comment
 * (PC)
 * <li>GET /api/reviews/submission/{id}/comments - Lấy internal comments
 * (PC/CHAIR/ADMIN)
 * <li>POST /api/reviews/rebuttal - Tạo/cập nhật rebuttal (AUTHOR)
 * <li>POST /api/reviews/rebuttal/{id}/submit - Submit rebuttal (AUTHOR)
 * <li>GET /api/reviews/rebuttal/submission/{id} - Lấy rebuttal (authenticated)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
  private final ReviewService reviewService;
  private final DiscussionService discussionService;
  private final UserRepository userRepository;

  public ReviewController(
      ReviewService reviewService,
      DiscussionService discussionService,
      UserRepository userRepository) {
    this.reviewService = reviewService;
    this.discussionService = discussionService;
    this.userRepository = userRepository;
  }

  @PostMapping("/draft")
  @PreAuthorize("hasRole('PC')")
  // Tạo hoặc cập nhật review nháp
  public ResponseEntity<ApiResponse<ReviewResponseDTO>> createOrUpdateDraft(
      @Valid @RequestBody ReviewSubmitDTO dto, Authentication authentication) {
    Long reviewerId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(reviewService.createOrUpdateDraft(dto, reviewerId)));
  }

  @PostMapping("/{id}/submit")
  @PreAuthorize("hasRole('PC')")
  // Submit review chính thức
  public ResponseEntity<ApiResponse<ReviewResponseDTO>> submitReview(
      @PathVariable Long id, Authentication authentication) {
    Long reviewerId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(reviewService.submitReview(id, reviewerId)));
  }

  @GetMapping("/assignment/{assignmentId}")
  @PreAuthorize("hasRole('PC')")
  // Lấy review của tôi cho assignment cụ thể
  public ResponseEntity<ApiResponse<ReviewResponseDTO>> getMyReview(
      @PathVariable Long assignmentId, Authentication authentication) {
    Long reviewerId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(reviewService.getMyReview(assignmentId, reviewerId)));
  }

  @GetMapping("/submission/{submissionId}")
  @PreAuthorize("isAuthenticated()")
  // Lấy danh sách reviews của một submission
  public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewsBySubmission(
      @PathVariable Long submissionId, Authentication authentication) {
    Long userId = getUserIdFromAuthentication(authentication);
    boolean isChairOrAdmin = isChairOrAdmin(userId);
    return ResponseEntity.ok(
        ApiResponse.success(
            reviewService.getReviewsBySubmission(submissionId, userId, isChairOrAdmin)));
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  // Lấy chi tiết một review
  public ResponseEntity<ApiResponse<ReviewResponseDTO>> getReview(
      @PathVariable Long id, Authentication authentication) {
    Long userId = getUserIdFromAuthentication(authentication);
    boolean isChairOrAdmin = isChairOrAdmin(userId);
    return ResponseEntity.ok(
        ApiResponse.success(reviewService.getReview(id, userId, isChairOrAdmin)));
  }

  @GetMapping("/submission/{submissionId}/average-score")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<AverageScoreDTO>> getAverageScore(
      @PathVariable Long submissionId, Authentication authentication) {
    return ResponseEntity.ok(
        ApiResponse.success(reviewService.getAverageScore(submissionId)));
  }

  @GetMapping("/conference/{conferenceId}/statistics")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<ReviewStatisticsDTO>> getReviewStatistics(
      @PathVariable Long conferenceId, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(reviewService.getReviewStatistics(conferenceId, chairId)));
  }

  // Internal Discussion

  @PostMapping("/submission/{submissionId}/comments")
  @PreAuthorize("hasRole('PC')")
  // Thêm comment thảo luận nội bộ (chỉ PC)
  public ResponseEntity<ApiResponse<ReviewCommentDTO>> addInternalComment(
      @PathVariable Long submissionId, @RequestBody String content, Authentication authentication) {
    Long reviewerId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(
            discussionService.addInternalComment(submissionId, content, reviewerId)));
  }

  @GetMapping("/submission/{submissionId}/comments")
  @PreAuthorize("hasRole('PC') or hasRole('CHAIR') or hasRole('ADMIN')")
  // Lấy danh sách internal comments (PC/Chair/Admin)
  public ResponseEntity<ApiResponse<List<ReviewCommentDTO>>> getInternalComments(
      @PathVariable Long submissionId, Authentication authentication) {
    Long userId = getUserIdFromAuthentication(authentication);
    boolean isChairOrAdmin = isChairOrAdmin(userId);
    return ResponseEntity.ok(
        ApiResponse.success(
            discussionService.getInternalComments(submissionId, userId, isChairOrAdmin)));
  }

  // Rebuttal

  @PostMapping("/rebuttal")
  @PreAuthorize("hasRole('AUTHOR')")
  // Tạo hoặc cập nhật phản biện (Author)
  public ResponseEntity<ApiResponse<RebuttalDTO>> createOrUpdateRebuttal(
      @Valid @RequestBody RebuttalSubmitDTO dto, Authentication authentication) {
    Long authorId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(discussionService.createOrUpdateRebuttal(dto, authorId)));
  }

  @PostMapping("/rebuttal/{id}/submit")
  @PreAuthorize("hasRole('AUTHOR')")
  // Nộp phản biện chính thức (Author)
  public ResponseEntity<ApiResponse<RebuttalDTO>> submitRebuttal(
      @PathVariable Long id, Authentication authentication) {
    Long authorId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(discussionService.submitRebuttal(id, authorId)));
  }

  @GetMapping("/rebuttal/submission/{submissionId}")
  @PreAuthorize("isAuthenticated()")
  // Lấy thông tin phản biện của submission
  public ResponseEntity<ApiResponse<RebuttalDTO>> getRebuttalBySubmission(
      @PathVariable Long submissionId, Authentication authentication) {
    Long userId = getUserIdFromAuthentication(authentication);
    boolean isChairOrAdmin = isChairOrAdmin(userId);
    RebuttalDTO rebuttal = discussionService.getRebuttalBySubmission(submissionId, userId, isChairOrAdmin);
    return ResponseEntity.ok(ApiResponse.success(rebuttal));
  }

  private Long getUserIdFromAuthentication(Authentication authentication) {
    String email = authentication.getName();
    User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    return user.getId();
  }

  @SuppressWarnings("null")
  private boolean isChairOrAdmin(Long userId) {
    User user = userRepository.findById(userId).orElse(null);
    if (user == null || user.getRoles() == null) {
      return false;
    }
    return user.getRoles().stream()
        .anyMatch(
            role -> role != null
                && (role.getName() == RoleName.CHAIR
                    || role.getName() == RoleName.ADMIN));
  }
}
