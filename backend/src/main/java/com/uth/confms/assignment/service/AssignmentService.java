package com.uth.confms.assignment.service;

import com.uth.confms.assignment.dto.AssignmentCreateDTO;
import com.uth.confms.assignment.dto.AssignmentResponseDTO;
import com.uth.confms.assignment.dto.AssignmentSuggestionDTO;
import com.uth.confms.assignment.dto.AssignmentStatisticsDTO;
import com.uth.confms.assignment.dto.AssignmentQualityMetricsDTO;
import com.uth.confms.assignment.dto.AutoAssignRequestDTO;
import com.uth.confms.assignment.dto.AutoAssignResponseDTO;
import com.uth.confms.assignment.dto.BulkAssignRequestDTO;
import com.uth.confms.assignment.dto.BulkAssignResponseDTO;
import com.uth.confms.assignment.dto.ReassignRequestDTO;
import com.uth.confms.assignment.entity.Assignment;
import com.uth.confms.assignment.repository.AssignmentRepository;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.auth.service.AuditLogService;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.conference.repository.DeadlineRepository;
import com.uth.confms.conference.entity.Deadline;
import com.uth.confms.pc.entity.PCMember;
import com.uth.confms.pc.repository.PCMemberRepository;
import com.uth.confms.pc.service.COIService;
import com.uth.confms.pc.service.WorkloadService;
import com.uth.confms.review.entity.Review;
import com.uth.confms.review.repository.ReviewRepository;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.repository.SubmissionRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý assignment (phân công reviewer cho submission)
 *
 * <p>
 * Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 * <li>Tạo assignment (chỉ chair)
 * <li>Reviewer accept/decline assignments
 * <li>Xóa assignments
 * <li>Kiểm tra COI trước khi assign
 * <li>Quản lý assignment status
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class AssignmentService {
  private final AssignmentRepository assignmentRepository;
  private final SubmissionRepository submissionRepository;
  private final UserRepository userRepository;
  private final PCMemberRepository pcMemberRepository;
  private final ConferenceRepository conferenceRepository;
  private final COIService coiService;
  private final WorkloadService workloadService;
  private final AssignmentSuggestionService suggestionService;
  private final AuditLogService auditLogService;
  private final ReviewRepository reviewRepository;
  private final DeadlineRepository deadlineRepository;

  public AssignmentService(
      AssignmentRepository assignmentRepository,
      SubmissionRepository submissionRepository,
      UserRepository userRepository,
      PCMemberRepository pcMemberRepository,
      ConferenceRepository conferenceRepository,
      COIService coiService,
      WorkloadService workloadService,
      AssignmentSuggestionService suggestionService,
      AuditLogService auditLogService,
      ReviewRepository reviewRepository,
      DeadlineRepository deadlineRepository) { // Added deadlineRepository
    this.assignmentRepository = assignmentRepository;
    this.submissionRepository = submissionRepository;
    this.userRepository = userRepository;
    this.pcMemberRepository = pcMemberRepository;
    this.conferenceRepository = conferenceRepository;
    this.coiService = coiService;
    this.workloadService = workloadService;
    this.suggestionService = suggestionService;
    this.auditLogService = auditLogService;
    this.reviewRepository = reviewRepository;
    this.deadlineRepository = deadlineRepository;
  }

  /**
   * Tạo phân công mới (chỉ Chair hoặc Admin có quyền).
   * Kiểm tra PC member status, COI và workload limit trước khi thực hiện.
   */
  @Transactional
  public AssignmentResponseDTO createAssignment(AssignmentCreateDTO dto, Long chairId, boolean isAdmin) {
    Submission submission = submissionRepository
        .findById(dto.getSubmissionId())
        .orElseThrow(
            () -> new NotFoundException(
                "Submission with id " + dto.getSubmissionId() + " not found"));

    Conference conference = conferenceRepository
        .findById(submission.getConferenceId())
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization - only chair (or admin) can assign
    if (!isAdmin && !conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair or admin can assign reviewers");
    }

    // Check if reviewer is a PC member
    PCMember pcMember = pcMemberRepository
        .findByConferenceIdAndUserId(submission.getConferenceId(), dto.getReviewerId())
        .orElseThrow(
            () -> new BusinessException("Reviewer must be a PC member of this conference"));

    if (pcMember.getStatus() != PCMember.PCMemberStatus.ACCEPTED) {
      throw new BusinessException("Reviewer must have accepted the PC invitation");
    }

    // Automatic COI detection: Check if reviewer is an author or has institutional
    // COI
    try {
      coiService.detectAndSuggestCOI(dto.getReviewerId(), dto.getSubmissionId());
    } catch (Exception e) {
      // Log error but continue - the explicit hasCOI check below will use what it can
      System.err.println("Failed to auto-detect COI: " + e.getMessage());
    }

    // Check for COI
    if (coiService.hasCOI(dto.getReviewerId(), dto.getSubmissionId())) {
      throw new BusinessException("Cannot assign reviewer with conflict of interest");
    }

    // Check workload limit
    if (workloadService.isOverloaded(dto.getReviewerId(), submission.getConferenceId())) {
      throw new BusinessException(
          "Cannot assign reviewer. Reviewer has reached the maximum workload limit ("
              + workloadService.getMaxAssignmentsPerReviewer()
              + " assignments).");
    }

    // Check if near limit (warning)
    boolean nearLimit = workloadService.isNearLimit(dto.getReviewerId(), submission.getConferenceId());
    if (nearLimit) {
      // Log warning but allow assignment
      System.out.println(
          "Warning: Reviewer "
              + dto.getReviewerId()
              + " is near workload limit in conference "
              + submission.getConferenceId());
    }

    // Check if assignment already exists
    if (assignmentRepository.existsBySubmissionIdAndReviewerId(
        dto.getSubmissionId(), dto.getReviewerId())) {
      throw new BusinessException("Assignment already exists for this reviewer and submission");
    }

    // Create assignment
    Assignment assignment = Assignment.builder()
        .submissionId(dto.getSubmissionId())
        .reviewerId(dto.getReviewerId())
        .status(Assignment.AssignmentStatus.ASSIGNED)
        .isPrimary(dto.getIsPrimary() != null ? dto.getIsPrimary() : false)
        .assignedAt(LocalDateTime.now())
        .build();

    assignment = assignmentRepository.save(assignment);

    // Automatic COI detection (moved up to validation phase)

    return mapToDTO(assignment);
  }

  /**
   * Reviewer chấp nhận phân công. Chuyển trạng thái sang ACCEPTED.
   * Nếu bài báo đang ở trạng thái SUBMITTED, tự động chuyển sang UNDER_REVIEW.
   */
  @Transactional
  public AssignmentResponseDTO acceptAssignment(Long assignmentId, Long reviewerId) {
    Assignment assignment = assignmentRepository
        .findById(assignmentId)
        .orElseThrow(() -> new NotFoundException("Assignment not found"));

    // Check authorization
    if (!assignment.getReviewerId().equals(reviewerId)) {
      throw new UnauthorizedException("You can only accept your own assignments");
    }

    if (assignment.getStatus() != Assignment.AssignmentStatus.ASSIGNED) {
      throw new BusinessException("Assignment is not in ASSIGNED status");
    }

    assignment.setStatus(Assignment.AssignmentStatus.ACCEPTED);
    assignment = assignmentRepository.save(assignment);

    // Automatically transition submission to UNDER_REVIEW if it's currently
    // SUBMITTED
    Submission submission = submissionRepository.findById(assignment.getSubmissionId()).orElse(null);
    if (submission != null && submission.getStatus() == Submission.SubmissionStatus.SUBMITTED) {
      submission.setStatus(Submission.SubmissionStatus.UNDER_REVIEW);
      submissionRepository.save(submission);
    }

    return mapToDTO(assignment);
  }

  /**
   * Reviewer từ chối phân công. Chuyển trạng thái sang DECLINED.
   * Nếu đây là reviewer cuối cùng phản hồi và các reviewer khác đã xong, cập nhật
   * trạng thái bài báo.
   */
  @Transactional
  public AssignmentResponseDTO declineAssignment(Long assignmentId, Long reviewerId) {
    Assignment assignment = assignmentRepository
        .findById(assignmentId)
        .orElseThrow(() -> new NotFoundException("Assignment not found"));

    // Check authorization
    if (!assignment.getReviewerId().equals(reviewerId)) {
      throw new UnauthorizedException("You can only decline your own assignments");
    }

    if (assignment.getStatus() != Assignment.AssignmentStatus.ASSIGNED) {
      throw new BusinessException("Assignment is not in ASSIGNED status");
    }

    assignment.setStatus(Assignment.AssignmentStatus.DECLINED);
    assignment = assignmentRepository.save(assignment);

    // If this was the last person to respond and all others submitted, transition
    // to
    // REVIEWED
    List<Assignment> allAssignments = assignmentRepository.findBySubmissionId(assignment.getSubmissionId());
    List<Assignment> activeAssignments = allAssignments.stream()
        .filter(a -> a.getStatus() != Assignment.AssignmentStatus.DECLINED)
        .collect(Collectors.toList());

    if (!activeAssignments.isEmpty() && activeAssignments.stream()
        .allMatch(a -> a.getStatus() == Assignment.AssignmentStatus.COMPLETED)) {
      Submission submission = submissionRepository.findById(assignment.getSubmissionId()).orElse(null);
      if (submission != null && (submission.getStatus() == Submission.SubmissionStatus.UNDER_REVIEW
          || submission.getStatus() == Submission.SubmissionStatus.SUBMITTED)) {
        submission.setStatus(Submission.SubmissionStatus.REVIEWED);
        submissionRepository.save(submission);
      }
    }

    return mapToDTO(assignment);
  }

  /**
   * Xóa phân công (chỉ Chair hoặc Admin).
   */
  @Transactional
  public void deleteAssignment(Long assignmentId, Long chairId, boolean isAdmin) {
    Assignment assignment = assignmentRepository
        .findById(assignmentId)
        .orElseThrow(() -> new NotFoundException("Assignment not found"));

    Submission submission = submissionRepository
        .findById(assignment.getSubmissionId())
        .orElseThrow(() -> new NotFoundException("Submission not found"));

    Conference conference = conferenceRepository
        .findById(submission.getConferenceId())
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization
    if (!isAdmin && !conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair or admin can delete assignments");
    }

    assignmentRepository.delete(assignment);
  }

  /**
   * Reassign assignment từ reviewer cũ sang reviewer mới
   *
   * @param assignmentId ID của assignment cần reassign
   * @param dto          Request DTO chứa newReviewerId và reason
   * @param chairId      ID của chair
   * @return AssignmentResponseDTO của assignment mới
   */
  @Transactional
  public AssignmentResponseDTO reassignAssignment(
      Long assignmentId, ReassignRequestDTO dto, Long chairId, boolean isAdmin) {
    Assignment oldAssignment = assignmentRepository
        .findById(assignmentId)
        .orElseThrow(() -> new NotFoundException("Assignment not found"));

    Submission submission = submissionRepository
        .findById(oldAssignment.getSubmissionId())
        .orElseThrow(() -> new NotFoundException("Submission not found"));

    Conference conference = conferenceRepository
        .findById(submission.getConferenceId())
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization
    if (!isAdmin && !conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair or admin can reassign assignments");
    }

    Long oldReviewerId = oldAssignment.getReviewerId();
    Long newReviewerId = dto.getNewReviewerId();

    // Check if new reviewer is different
    if (oldReviewerId.equals(newReviewerId)) {
      throw new BusinessException("New reviewer must be different from current reviewer");
    }

    // Validate new reviewer (same validations as createAssignment)
    PCMember newPCMember = pcMemberRepository
        .findByConferenceIdAndUserId(submission.getConferenceId(), newReviewerId)
        .orElseThrow(
            () -> new BusinessException("New reviewer must be a PC member of this conference"));

    if (newPCMember.getStatus() != PCMember.PCMemberStatus.ACCEPTED) {
      throw new BusinessException("New reviewer must have accepted the PC invitation");
    }

    // Automatic COI detection
    try {
      coiService.detectAndSuggestCOI(newReviewerId, submission.getId());
    } catch (Exception e) {
      System.err.println("Failed to auto-detect COI during reassignment: " + e.getMessage());
    }

    // Check for COI
    if (coiService.hasCOI(newReviewerId, submission.getId())) {
      throw new BusinessException("Cannot reassign to reviewer with conflict of interest");
    }

    // Check workload limit
    if (workloadService.isOverloaded(newReviewerId, submission.getConferenceId())) {
      throw new BusinessException(
          "Cannot reassign to reviewer. Reviewer has reached the maximum workload limit ("
              + workloadService.getMaxAssignmentsPerReviewer()
              + " assignments).");
    }

    // Check if assignment already exists for new reviewer
    if (assignmentRepository.existsBySubmissionIdAndReviewerId(
        submission.getId(), newReviewerId)) {
      throw new BusinessException("Assignment already exists for new reviewer and submission");
    }

    // Delete old assignment
    assignmentRepository.delete(oldAssignment);

    Assignment newAssignment = Assignment.builder()
        .submissionId(submission.getId())
        .reviewerId(newReviewerId)
        .status(Assignment.AssignmentStatus.ASSIGNED)
        .isPrimary(oldAssignment.getIsPrimary())
        .assignedAt(LocalDateTime.now())
        .build();

    newAssignment = assignmentRepository.save(newAssignment);

    // Automatic COI detection (moved up to validation phase)

    // Audit logging
    try {
      String reason = dto.getReason() != null ? dto.getReason() : "No reason provided";
      auditLogService.logAction(
          chairId,
          "ASSIGNMENT_REASSIGNED",
          "ASSIGNMENT",
          newAssignment.getId(),
          String.format(
              "Assignment reassigned from reviewer %d to reviewer %d for submission %d. Reason: %s",
              oldReviewerId, newReviewerId, submission.getId(), reason));
    } catch (Exception e) {
      // Don't fail reassignment if audit logging fails
      System.err.println("Failed to log reassignment audit: " + e.getMessage());
    }

    return mapToDTO(newAssignment);
  }

  public List<AssignmentResponseDTO> getAssignmentsBySubmission(Long submissionId, Long chairId, boolean isAdmin) {
    Submission submission = submissionRepository
        .findById(submissionId)
        .orElseThrow(() -> new NotFoundException("Submission not found"));

    Conference conference = conferenceRepository
        .findById(submission.getConferenceId())
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization
    if (!isAdmin && !conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair or admin can view assignments");
    }

    return mapToDTOList(assignmentRepository.findBySubmissionId(submissionId));
  }

  public List<AssignmentResponseDTO> getMyAssignments(Long reviewerId) {
    return mapToDTOList(assignmentRepository.findByReviewerId(reviewerId));
  }

  public AssignmentResponseDTO getAssignment(Long assignmentId, Long userId) {
    Assignment assignment = assignmentRepository
        .findById(assignmentId)
        .orElseThrow(() -> new NotFoundException("Assignment not found"));

    Submission submission = submissionRepository
        .findById(assignment.getSubmissionId())
        .orElseThrow(() -> new NotFoundException("Submission not found"));

    Conference conference = conferenceRepository
        .findById(submission.getConferenceId())
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization - reviewer or chair
    if (!assignment.getReviewerId().equals(userId) && !conference.getChairId().equals(userId)) {
      throw new UnauthorizedException("You don't have permission to view this assignment");
    }

    return mapToDTO(assignment);
  }

  /**
   * Tự động assign reviewers cho submission dựa trên suggestions
   *
   * @param dto     Request DTO chứa submissionId và numberOfReviewers
   * @param chairId ID của chair
   * @return AutoAssignResponseDTO chứa danh sách assignments đã tạo và failed
   *         assignments
   */
  @Transactional
  public AutoAssignResponseDTO autoAssign(AutoAssignRequestDTO dto, Long chairId, boolean isAdmin) {
    Submission submission = submissionRepository
        .findById(dto.getSubmissionId())
        .orElseThrow(
            () -> new NotFoundException(
                "Submission with id " + dto.getSubmissionId() + " not found"));

    Conference conference = conferenceRepository
        .findById(submission.getConferenceId())
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization
    if (!isAdmin && !conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair or admin can auto-assign reviewers");
    }

    // Get suggestions
    List<AssignmentSuggestionDTO> suggestions = suggestionService.getSuggestions(dto.getSubmissionId());

    // Take top N suggestions
    int numberOfReviewers = dto.getNumberOfReviewers() != null ? dto.getNumberOfReviewers() : 3;
    List<AssignmentSuggestionDTO> topSuggestions = suggestions.stream().limit(numberOfReviewers)
        .collect(Collectors.toList());

    List<AssignmentResponseDTO> createdAssignments = new ArrayList<>();
    List<AutoAssignResponseDTO.FailedAssignmentDTO> failedAssignments = new ArrayList<>();

    // Try to assign each suggestion
    for (AssignmentSuggestionDTO suggestion : topSuggestions) {
      try {
        AssignmentCreateDTO createDTO = new AssignmentCreateDTO();
        createDTO.setSubmissionId(dto.getSubmissionId());
        createDTO.setReviewerId(suggestion.getReviewerId());
        createDTO.setIsPrimary(false);

        AssignmentResponseDTO assignment = createAssignment(createDTO, chairId, isAdmin);
        createdAssignments.add(assignment);
      } catch (Exception e) {
        User reviewer = userRepository.findById(suggestion.getReviewerId()).orElse(null);
        failedAssignments.add(
            new AutoAssignResponseDTO.FailedAssignmentDTO(
                suggestion.getReviewerId(),
                reviewer != null ? reviewer.getEmail() : null,
                reviewer != null ? reviewer.getFullName() : null,
                e.getMessage()));
      }
    }

    return new AutoAssignResponseDTO(
        createdAssignments,
        failedAssignments,
        numberOfReviewers,
        createdAssignments.size(),
        failedAssignments.size());
  }

  /**
   * Bulk assign reviewers cho nhiều submissions
   *
   * @param dto     Request DTO chứa danh sách assignments
   * @param chairId ID của chair
   * @return BulkAssignResponseDTO chứa danh sách assignments đã tạo và failed
   *         assignments
   */
  @Transactional
  public BulkAssignResponseDTO bulkAssign(BulkAssignRequestDTO dto, Long chairId, boolean isAdmin) {
    List<AssignmentResponseDTO> createdAssignments = new ArrayList<>();
    List<BulkAssignResponseDTO.FailedAssignmentDTO> failedAssignments = new ArrayList<>();

    // Try to assign each assignment
    for (AssignmentCreateDTO assignmentDTO : dto.getAssignments()) {
      try {
        AssignmentResponseDTO assignment = createAssignment(assignmentDTO, chairId, isAdmin);
        createdAssignments.add(assignment);
      } catch (Exception e) {
        failedAssignments.add(
            new BulkAssignResponseDTO.FailedAssignmentDTO(
                assignmentDTO.getSubmissionId(),
                assignmentDTO.getReviewerId(),
                e.getMessage()));
      }
    }

    return new BulkAssignResponseDTO(
        createdAssignments,
        failedAssignments,
        dto.getAssignments().size(),
        createdAssignments.size(),
        failedAssignments.size());
  }

  /**
   * Lấy assignment statistics cho một conference
   *
   * @param conferenceId ID của conference
   * @param chairId      ID của chair
   * @return AssignmentStatisticsDTO chứa các thống kê
   */
  public AssignmentStatisticsDTO getAssignmentStatistics(Long conferenceId, Long chairId, boolean isAdmin) {
    Conference conference = conferenceRepository
        .findById(conferenceId)
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization
    if (!isAdmin && !conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair or admin can view assignment statistics");
    }

    // Get all submissions for this conference
    List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
    List<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());

    // Get all assignments for these submissions in one query
    List<Assignment> allAssignments = assignmentRepository.findBySubmissionIdIn(submissionIds);

    // Calculate statistics
    int totalAssignments = allAssignments.size();

    // Get unique reviewers
    Set<Long> reviewerIds = allAssignments.stream()
        .map(Assignment::getReviewerId)
        .collect(Collectors.toSet());
    int totalReviewers = reviewerIds.size();

    // Calculate average assignments per reviewer
    double averageAssignmentsPerReviewer = totalReviewers > 0
        ? (double) totalAssignments / totalReviewers
        : 0.0;

    // Calculate min/max assignments per reviewer
    int minAssignments = Integer.MAX_VALUE;
    int maxAssignments = 0;
    Map<Long, Integer> reviewerAssignmentCounts = new HashMap<>();
    for (Assignment assignment : allAssignments) {
      reviewerAssignmentCounts.merge(assignment.getReviewerId(), 1, Integer::sum);
    }
    for (Integer count : reviewerAssignmentCounts.values()) {
      minAssignments = Math.min(minAssignments, count);
      maxAssignments = Math.max(maxAssignments, count);
    }
    if (minAssignments == Integer.MAX_VALUE) {
      minAssignments = 0;
    }

    // Status distribution
    Map<String, Integer> statusDistribution = new HashMap<>();
    statusDistribution.put("ASSIGNED", 0);
    statusDistribution.put("ACCEPTED", 0);
    statusDistribution.put("DECLINED", 0);
    statusDistribution.put("COMPLETED", 0);
    for (Assignment assignment : allAssignments) {
      statusDistribution.merge(assignment.getStatus().name(), 1, Integer::sum);
    }

    // Workload distribution (using WorkloadService)
    Map<String, Integer> workloadDistribution = new HashMap<>();
    workloadDistribution.put("LOW", 0);
    workloadDistribution.put("NORMAL", 0);
    workloadDistribution.put("HIGH", 0);
    workloadDistribution.put("OVERLOADED", 0);

    for (Map.Entry<Long, Integer> entry : reviewerAssignmentCounts.entrySet()) {
      String status = workloadService.calculateWorkloadStatus(entry.getValue());
      workloadDistribution.merge(status, 1, Integer::sum);
    }

    // Calculate rates
    int acceptedCount = statusDistribution.get("ACCEPTED");
    int completedCount = statusDistribution.get("COMPLETED");
    int declinedCount = statusDistribution.get("DECLINED");
    int respondedCount = acceptedCount + declinedCount; // Assignments that got response

    double acceptanceRate = respondedCount > 0
        ? (double) acceptedCount / respondedCount * 100.0
        : 0.0;
    double completionRate = acceptedCount > 0
        ? (double) completedCount / acceptedCount * 100.0
        : 0.0;
    double declineRate = respondedCount > 0
        ? (double) declinedCount / respondedCount * 100.0
        : 0.0;

    return new AssignmentStatisticsDTO(
        totalAssignments,
        totalReviewers,
        averageAssignmentsPerReviewer,
        minAssignments,
        maxAssignments,
        statusDistribution,
        workloadDistribution,
        acceptanceRate,
        completionRate,
        declineRate);
  }

  /**
   * Lấy assignment quality metrics cho một conference
   *
   * @param conferenceId ID của conference
   * @param chairId      ID của chair
   * @return AssignmentQualityMetricsDTO chứa các quality metrics
   */
  public AssignmentQualityMetricsDTO getAssignmentQualityMetrics(Long conferenceId, Long chairId, boolean isAdmin) {
    Conference conference = conferenceRepository
        .findById(conferenceId)
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization
    // Check authorization
    if (!isAdmin && !conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair or admin can view assignment quality metrics");
    }

    // Get all submissions for this conference
    List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
    List<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());

    // Get all assignments in one query
    List<Assignment> allAssignments = assignmentRepository.findBySubmissionIdIn(submissionIds);

    // Get all reviews for these assignments
    List<Long> assignmentIds = allAssignments.stream().map(Assignment::getId).collect(Collectors.toList());
    // Get all reviews for these assignments in one query
    List<Review> allReviews = reviewRepository.findByAssignmentIdIn(assignmentIds);

    // Calculate average review score
    List<Review> submittedReviews = allReviews.stream()
        .filter(r -> r.getStatus() == Review.ReviewStatus.SUBMITTED && r.getScore() != null)
        .collect(Collectors.toList());

    double averageReviewScore = 0.0;
    if (!submittedReviews.isEmpty()) {
      double totalScore = 0.0;
      for (Review review : submittedReviews) {
        totalScore += getReviewScoreValue(review.getScore());
      }
      averageReviewScore = totalScore / submittedReviews.size();
    }

    // Review score distribution
    Map<String, Integer> reviewScoreDistribution = new HashMap<>();
    reviewScoreDistribution.put("STRONG_ACCEPT", 0);
    reviewScoreDistribution.put("ACCEPT", 0);
    reviewScoreDistribution.put("WEAK_ACCEPT", 0);
    reviewScoreDistribution.put("BORDERLINE", 0);
    reviewScoreDistribution.put("WEAK_REJECT", 0);
    reviewScoreDistribution.put("REJECT", 0);
    reviewScoreDistribution.put("STRONG_REJECT", 0);

    for (Review review : submittedReviews) {
      if (review.getScore() != null) {
        reviewScoreDistribution.merge(review.getScore().name(), 1, Integer::sum);
      }
    }

    // Calculate average review completion time
    double averageReviewCompletionTime = 0.0;
    List<Long> completionTimes = new ArrayList<>();
    for (Review review : submittedReviews) {
      if (review.getSubmittedAt() != null && review.getCreatedAt() != null) {
        Duration duration = Duration.between(review.getCreatedAt(), review.getSubmittedAt());
        completionTimes.add(duration.toDays());
      }
    }
    if (!completionTimes.isEmpty()) {
      averageReviewCompletionTime = completionTimes.stream()
          .mapToLong(Long::longValue)
          .average()
          .orElse(0.0);
    }

    // Review submission rate
    int totalAcceptedAssignments = (int) allAssignments.stream()
        .filter(a -> a.getStatus() == Assignment.AssignmentStatus.ACCEPTED)
        .count();
    int totalReviewsSubmitted = submittedReviews.size();
    double reviewSubmissionRate = totalAcceptedAssignments > 0
        ? (double) totalReviewsSubmitted / totalAcceptedAssignments * 100.0
        : 0.0;

    // Total reviews pending
    int totalReviewsPending = (int) allReviews.stream()
        .filter(r -> r.getStatus() == Review.ReviewStatus.DRAFT)
        .count();

    // Average reviewer rating (based on review quality)
    Set<Long> reviewerIds = allAssignments.stream()
        .map(Assignment::getReviewerId)
        .collect(Collectors.toSet());

    // Batch fetch all reviews for these reviewers to avoid N+1
    Map<Long, List<Review>> reviewerReviewsMap = reviewRepository.findByReviewerIdIn(reviewerIds).stream()
        .filter(r -> r.getStatus() == Review.ReviewStatus.SUBMITTED && r.getScore() != null)
        .collect(Collectors.groupingBy(Review::getReviewerId));

    double totalReviewerRating = 0.0;
    int reviewersWithReviews = 0;
    for (Long reviewerId : reviewerIds) {
      List<Review> reviewerReviews = reviewerReviewsMap.getOrDefault(reviewerId, List.of());

      if (!reviewerReviews.isEmpty()) {
        double reviewerAvgScore = reviewerReviews.stream()
            .mapToDouble(r -> getReviewScoreValue(r.getScore()))
            .average()
            .orElse(0.0);
        totalReviewerRating += reviewerAvgScore / 7.0; // Normalize to 0.0-1.0
        reviewersWithReviews++;
      }
    }

    double averageReviewerRating = reviewersWithReviews > 0
        ? totalReviewerRating / reviewersWithReviews
        : 0.0;

    return new AssignmentQualityMetricsDTO(
        averageReviewScore,
        reviewScoreDistribution,
        averageReviewCompletionTime,
        totalReviewsSubmitted,
        totalReviewsPending,
        reviewSubmissionRate,
        averageReviewerRating);
  }

  /**
   * Convert ReviewScore enum to numeric value
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

  private AssignmentResponseDTO mapToDTO(Assignment assignment) {
    Submission submission = submissionRepository.findById(assignment.getSubmissionId()).orElse(null);
    User reviewer = userRepository.findById(assignment.getReviewerId()).orElse(null);

    LocalDateTime reviewDeadline = null;
    if (submission != null) {
      List<Deadline> deadlines = deadlineRepository.findByConferenceId(submission.getConferenceId());
      if (deadlines != null && !deadlines.isEmpty()) {
        // Find review deadline by type
        reviewDeadline = deadlines.stream()
            .filter(d -> d.getType() == Deadline.DeadlineType.REVIEW)
            .map(Deadline::getDueDate)
            .max(LocalDateTime::compareTo)
            .orElse(null);

        // Fallback: if no review deadline, take the latest deadline
        if (reviewDeadline == null) {
          reviewDeadline = deadlines.stream()
              .map(Deadline::getDueDate)
              .max(LocalDateTime::compareTo)
              .orElse(null);
        }
      }
    }

    return AssignmentResponseDTO.builder()
        .id(assignment.getId())
        .submissionId(assignment.getSubmissionId())
        .submissionTitle(submission != null ? submission.getTitle() : null)
        .reviewerId(assignment.getReviewerId())
        .reviewerEmail(reviewer != null ? reviewer.getEmail() : null)
        .reviewerName(reviewer != null ? reviewer.getFullName() : null)
        .status(assignment.getStatus().name())
        .isPrimary(assignment.getIsPrimary())
        .submissionAbstract(submission != null ? submission.getAbstractText() : null)
        .assignedAt(assignment.getAssignedAt())
        .updatedAt(assignment.getUpdatedAt())
        .deadline(reviewDeadline)
        .build();
  }

  private List<AssignmentResponseDTO> mapToDTOList(List<Assignment> assignments) {
    if (assignments == null || assignments.isEmpty()) {
      return new ArrayList<>();
    }

    // Collect all unique IDs
    List<Long> submissionIds = assignments.stream()
        .map(Assignment::getSubmissionId)
        .distinct()
        .collect(Collectors.toList());

    List<Long> reviewerIds = assignments.stream()
        .map(Assignment::getReviewerId)
        .distinct()
        .collect(Collectors.toList());

    // Batch fetch related entities
    Map<Long, Submission> submissionMap = submissionRepository.findAllById(submissionIds).stream()
        .collect(Collectors.toMap(Submission::getId, s -> s));

    Map<Long, User> reviewerMap = userRepository.findAllById(reviewerIds).stream()
        .collect(Collectors.toMap(User::getId, u -> u));

    // Collect conference IDs from submissions to fetch deadlines
    List<Long> conferenceIds = submissionMap.values().stream()
        .map(Submission::getConferenceId)
        .distinct()
        .collect(Collectors.toList());

    Map<Long, List<Deadline>> deadlineMap = deadlineRepository.findByConferenceIdIn(conferenceIds).stream()
        .collect(Collectors.groupingBy(d -> d.getConference().getId()));

    return assignments.stream()
        .map(assignment -> {
          Submission submission = submissionMap.get(assignment.getSubmissionId());
          User reviewer = reviewerMap.get(assignment.getReviewerId());

          LocalDateTime reviewDeadline = null;
          if (submission != null) {
            List<Deadline> deadlines = deadlineMap.getOrDefault(submission.getConferenceId(), new ArrayList<>());
            if (!deadlines.isEmpty()) {
              reviewDeadline = deadlines.stream()
                  .filter(d -> d.getType() == Deadline.DeadlineType.REVIEW)
                  .map(Deadline::getDueDate)
                  .max(LocalDateTime::compareTo)
                  .orElse(null);

              if (reviewDeadline == null) {
                reviewDeadline = deadlines.stream()
                    .map(Deadline::getDueDate)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
              }
            }
          }

          return AssignmentResponseDTO.builder()
              .id(assignment.getId())
              .submissionId(assignment.getSubmissionId())
              .submissionTitle(submission != null ? submission.getTitle() : null)
              .reviewerId(assignment.getReviewerId())
              .reviewerEmail(reviewer != null ? reviewer.getEmail() : null)
              .reviewerName(reviewer != null ? reviewer.getFullName() : null)
              .status(assignment.getStatus().name())
              .isPrimary(assignment.getIsPrimary())
              .submissionAbstract(submission != null ? submission.getAbstractText() : null)
              .assignedAt(assignment.getAssignedAt())
              .updatedAt(assignment.getUpdatedAt())
              .deadline(reviewDeadline)
              .build();
        })
        .collect(Collectors.toList());
  }
}
