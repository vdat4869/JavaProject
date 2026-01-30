package com.uth.confms.review.service;

import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.entity.Deadline;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.conference.repository.DeadlineRepository;
import com.uth.confms.pc.entity.PCMember;
import com.uth.confms.pc.repository.PCMemberRepository;
import com.uth.confms.review.dto.RebuttalDTO;
import com.uth.confms.review.dto.RebuttalSubmitDTO;
import com.uth.confms.review.dto.ReviewCommentDTO;
import com.uth.confms.review.entity.Rebuttal;
import com.uth.confms.review.entity.ReviewComment;
import com.uth.confms.review.repository.RebuttalRepository;
import com.uth.confms.review.repository.ReviewCommentRepository;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.repository.SubmissionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý internal discussions và rebuttals
 *
 * <p>
 * Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 * <li>Internal discussion giữa reviewers (chỉ PC members)
 * <li>Author rebuttals (phản hồi reviews)
 * <li>Quản lý review comments
 * <li>Authorization cho discussions
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class DiscussionService {
  private final ReviewCommentRepository commentRepository;
  private final RebuttalRepository rebuttalRepository;
  private final SubmissionRepository submissionRepository;
  private final UserRepository userRepository;
  private final PCMemberRepository pcMemberRepository;
  private final ConferenceRepository conferenceRepository;
  private final DeadlineRepository deadlineRepository;

  public DiscussionService(
      ReviewCommentRepository commentRepository,
      RebuttalRepository rebuttalRepository,
      SubmissionRepository submissionRepository,
      UserRepository userRepository,
      PCMemberRepository pcMemberRepository,
      ConferenceRepository conferenceRepository,
      DeadlineRepository deadlineRepository) {
    this.commentRepository = commentRepository;
    this.rebuttalRepository = rebuttalRepository;
    this.submissionRepository = submissionRepository;
    this.userRepository = userRepository;
    this.pcMemberRepository = pcMemberRepository;
    this.conferenceRepository = conferenceRepository;
    this.deadlineRepository = deadlineRepository;
  }

  /**
   * Thêm internal comment (thảo luận nội bộ giữa reviewers)
   *
   * @param submissionId ID của submission
   * @param content      Nội dung comment
   * @param reviewerId   ID của reviewer thêm comment
   * @return ReviewCommentDTO chứa thông tin comment đã tạo
   * @throws NotFoundException     Nếu không tìm thấy submission
   * @throws UnauthorizedException Nếu reviewer không phải PC member
   */
  @Transactional
  // Thêm internal comment (thảo luận nội bộ giữa reviewers)
  public ReviewCommentDTO addInternalComment(Long submissionId, String content, Long reviewerId) {
    Submission submission = submissionRepository
        .findById(submissionId)
        .orElseThrow(() -> new NotFoundException("Submission not found"));

    // Check if reviewer is PC member
    PCMember pcMember = pcMemberRepository
        .findByConferenceIdAndUserId(submission.getConferenceId(), reviewerId)
        .orElseThrow(
            () -> new UnauthorizedException("Only PC members can add internal comments"));

    if (pcMember.getStatus() != PCMember.PCMemberStatus.ACCEPTED) {
      throw new UnauthorizedException("PC member must be accepted");
    }

    // Check review deadline
    checkReviewDeadline(submission.getConferenceId());

    ReviewComment comment = ReviewComment.builder()
        .submissionId(submissionId)
        .reviewerId(reviewerId)
        .content(content)
        .isInternal(true)
        .build();

    comment = commentRepository.save(comment);

    return mapCommentToDTO(comment, true); // true = show reviewer name for internal comments
  }

  // Lấy danh sách internal comments
  public List<ReviewCommentDTO> getInternalComments(
      Long submissionId, Long userId, boolean isChairOrAdmin) {
    Submission submission = submissionRepository
        .findById(submissionId)
        .orElseThrow(() -> new NotFoundException("Submission not found"));

    // Check authorization: only PC members, chair, or admin can see internal
    // comments
    Conference conference = conferenceRepository
        .findById(submission.getConferenceId())
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    boolean isPC = pcMemberRepository
        .findByConferenceIdAndUserId(submission.getConferenceId(), userId)
        .isPresent();

    if (!isPC && !isChairOrAdmin && !conference.getChairId().equals(userId)) {
      throw new UnauthorizedException(
          "Only PC members, chair, or admin can view internal comments");
    }

    List<ReviewComment> comments = commentRepository.findBySubmissionIdAndIsInternalTrue(submissionId);

    return comments.stream()
        .map(
            comment -> mapCommentToDTO(comment, true)) // Show reviewer names for internal discussion
        .collect(Collectors.toList());
  }

  /**
   * Tạo hoặc cập nhật rebuttal (phản hồi của author)
   *
   * @param dto      Thông tin rebuttal (submissionId, content)
   * @param authorId ID của author tạo rebuttal
   * @return RebuttalDTO chứa thông tin rebuttal
   * @throws NotFoundException     Nếu không tìm thấy submission
   * @throws UnauthorizedException Nếu không phải author của submission
   * @throws BusinessException     Nếu rebuttal đã được submit
   */
  @Transactional
  // Tạo hoặc cập nhật rebuttal (phản hồi của author)
  public RebuttalDTO createOrUpdateRebuttal(RebuttalSubmitDTO dto, Long authorId) {
    Submission submission = submissionRepository
        .findById(dto.getSubmissionId())
        .orElseThrow(() -> new NotFoundException("Submission not found"));

    // Check authorization - only submission author can create rebuttal
    if (!submission.getAuthorId().equals(authorId)) {
      throw new UnauthorizedException("Only submission author can create rebuttal");
    }

    // Check if rebuttal already exists
    Rebuttal rebuttal = rebuttalRepository.findBySubmissionId(dto.getSubmissionId()).orElse(null);

    if (rebuttal == null) {
      // Create new rebuttal
      rebuttal = Rebuttal.builder()
          .submissionId(dto.getSubmissionId())
          .authorId(authorId)
          .content(dto.getContent())
          .status(Rebuttal.RebuttalStatus.DRAFT)
          .build();
    } else {
      // Update existing rebuttal (only if still in DRAFT status)
      if (rebuttal.getStatus() != Rebuttal.RebuttalStatus.DRAFT) {
        throw new BusinessException("Cannot update submitted rebuttal");
      }

      if (!rebuttal.getAuthorId().equals(authorId)) {
        throw new UnauthorizedException("You can only update your own rebuttal");
      }

      rebuttal.setContent(dto.getContent());
    }

    rebuttal = rebuttalRepository.save(rebuttal);

    return mapRebuttalToDTO(rebuttal);
  }

  @Transactional
  // Submit rebuttal (chuyển trạng thái sang SUBMITTED)
  public RebuttalDTO submitRebuttal(Long rebuttalId, Long authorId) {
    Rebuttal rebuttal = rebuttalRepository
        .findById(rebuttalId)
        .orElseThrow(() -> new NotFoundException("Rebuttal not found"));

    // Check authorization
    if (!rebuttal.getAuthorId().equals(authorId)) {
      throw new UnauthorizedException("You can only submit your own rebuttal");
    }

    // Check status
    if (rebuttal.getStatus() != Rebuttal.RebuttalStatus.DRAFT) {
      throw new BusinessException("Rebuttal is already submitted");
    }

    rebuttal.setStatus(Rebuttal.RebuttalStatus.SUBMITTED);
    rebuttal.setSubmittedAt(LocalDateTime.now());
    rebuttal = rebuttalRepository.save(rebuttal);

    return mapRebuttalToDTO(rebuttal);
  }

  // Lấy rebuttal của submission
  public RebuttalDTO getRebuttalBySubmission(
      Long submissionId, Long userId, boolean isChairOrAdmin) {
    Submission submission = submissionRepository
        .findById(submissionId)
        .orElseThrow(() -> new NotFoundException("Submission not found"));

    Rebuttal rebuttal = rebuttalRepository.findBySubmissionId(submissionId).orElse(null);

    if (rebuttal == null) {
      return null;
    }

    // Check authorization: author, reviewers, chair, or admin can view
    boolean isAuthor = submission.getAuthorId().equals(userId);
    boolean isPC = pcMemberRepository
        .findByConferenceIdAndUserId(submission.getConferenceId(), userId)
        .isPresent();

    if (!isAuthor && !isPC && !isChairOrAdmin) {
      throw new UnauthorizedException("You don't have permission to view this rebuttal");
    }

    return mapRebuttalToDTO(rebuttal);
  }

  /**
   * Check if review deadline has passed
   *
   * @param conferenceId Conference ID
   * @throws BusinessException If deadline has passed and is hard deadline
   */
  // Kiểm tra deadline review
  private void checkReviewDeadline(Long conferenceId) {
    List<Deadline> deadlines = deadlineRepository.findByConferenceId(conferenceId);
    Deadline reviewDeadline = deadlines.stream()
        .filter(d -> d.getType() == Deadline.DeadlineType.REVIEW)
        .findFirst()
        .orElse(null);

    if (reviewDeadline != null && reviewDeadline.getDueDate().isBefore(LocalDateTime.now())) {
      if (reviewDeadline.getHardDeadline()) {
        throw new BusinessException("Review deadline has passed. Cannot add internal comment.");
      }
    }
  }

  private ReviewCommentDTO mapCommentToDTO(ReviewComment comment, boolean showReviewerName) {
    User reviewer = showReviewerName ? userRepository.findById(comment.getReviewerId()).orElse(null) : null;

    return ReviewCommentDTO.builder()
        .id(comment.getId())
        .submissionId(comment.getSubmissionId())
        .reviewerId(comment.getReviewerId())
        .reviewerName(reviewer != null ? reviewer.getFullName() : null)
        .content(comment.getContent())
        .isInternal(comment.getIsInternal())
        .createdAt(comment.getCreatedAt())
        .updatedAt(comment.getUpdatedAt())
        .build();
  }

  private RebuttalDTO mapRebuttalToDTO(Rebuttal rebuttal) {
    return RebuttalDTO.builder()
        .id(rebuttal.getId())
        .submissionId(rebuttal.getSubmissionId())
        .authorId(rebuttal.getAuthorId())
        .content(rebuttal.getContent())
        .status(rebuttal.getStatus().name())
        .createdAt(rebuttal.getCreatedAt())
        .submittedAt(rebuttal.getSubmittedAt())
        .build();
  }
}
