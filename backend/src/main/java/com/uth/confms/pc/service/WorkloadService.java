package com.uth.confms.pc.service;

import com.uth.confms.assignment.entity.Assignment;
import com.uth.confms.assignment.repository.AssignmentRepository;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.pc.dto.WorkloadDTO;
import com.uth.confms.pc.dto.WorkloadStatsDTO;
import com.uth.confms.pc.entity.PCMember;
import com.uth.confms.pc.repository.PCMemberRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service quản lý workload tracking cho reviewers
 *
 * <p>
 * Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 * <li>Lấy workload của một reviewer
 * <li>Lấy workload statistics của một conference
 * <li>Tính toán workload status (LOW, NORMAL, HIGH, OVERLOADED)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class WorkloadService {
  private final AssignmentRepository assignmentRepository;
  private final PCMemberRepository pcMemberRepository;
  private final UserRepository userRepository;
  private final ConferenceRepository conferenceRepository;

  @Value("${app.pc.workload.max-assignments:8}")
  private int maxAssignmentsPerReviewer;

  @Value("${app.pc.workload.warning-threshold:0.8}")
  private double warningThreshold; // 80% of max

  public WorkloadService(
      AssignmentRepository assignmentRepository,
      PCMemberRepository pcMemberRepository,
      UserRepository userRepository,
      ConferenceRepository conferenceRepository) {
    this.assignmentRepository = assignmentRepository;
    this.pcMemberRepository = pcMemberRepository;
    this.userRepository = userRepository;
    this.conferenceRepository = conferenceRepository;
  }

  /**
   * Lấy workload của một reviewer trong một conference
   *
   * @param reviewerId   ID của reviewer
   * @param conferenceId ID của conference
   * @return WorkloadDTO chứa thông tin workload
   */
  /**
   * Lấy workload của một reviewer trong một conference
   *
   * @param reviewerId   ID của reviewer
   * @param conferenceId ID của conference
   * @return WorkloadDTO chứa thông tin workload
   */
  // Lấy chi tiết workload của 1 reviewer
  public WorkloadDTO getReviewerWorkload(Long reviewerId, Long conferenceId) {
    Conference conference = conferenceRepository.findById(conferenceId)
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    User reviewer = userRepository.findById(reviewerId)
        .orElseThrow(() -> new NotFoundException("Reviewer not found"));

    // Optimized: Direct query to get conference assignments for this reviewer
    List<Assignment> conferenceAssignments = assignmentRepository.findByReviewerIdAndConferenceId(reviewerId,
        conferenceId);

    // Count assignments by status for this conference
    long assignedCount = conferenceAssignments.stream()
        .filter(a -> a.getStatus() == Assignment.AssignmentStatus.ASSIGNED)
        .count();
    long acceptedCount = conferenceAssignments.stream()
        .filter(a -> a.getStatus() == Assignment.AssignmentStatus.ACCEPTED)
        .count();
    long declinedCount = conferenceAssignments.stream()
        .filter(a -> a.getStatus() == Assignment.AssignmentStatus.DECLINED)
        .count();
    long completedCount = conferenceAssignments.stream()
        .filter(a -> a.getStatus() == Assignment.AssignmentStatus.COMPLETED)
        .count();

    long totalAssignments = assignedCount + acceptedCount + completedCount; // Exclude declined

    // Calculate workload status
    String workloadStatus = calculateWorkloadStatus(totalAssignments);
    double workloadPercentage = (double) totalAssignments / maxAssignmentsPerReviewer * 100.0;

    return WorkloadDTO.builder()
        .reviewerId(reviewerId)
        .reviewerEmail(reviewer.getEmail())
        .reviewerName(reviewer.getFullName())
        .conferenceId(conferenceId)
        .conferenceName(conference.getName())
        .totalAssignments(totalAssignments)
        .assignedCount(assignedCount)
        .acceptedCount(acceptedCount)
        .declinedCount(declinedCount)
        .completedCount(completedCount)
        .workloadStatus(workloadStatus)
        .maxAssignments(maxAssignmentsPerReviewer)
        .workloadPercentage(workloadPercentage)
        .build();
  }

  /**
   * Lấy workload statistics của một conference
   *
   * @param conferenceId ID của conference
   * @param chairId      ID của chair (for authorization)
   * @return WorkloadStatsDTO chứa thông tin statistics
   */
  /**
   * Lấy workload statistics của một conference
   *
   * @param conferenceId ID của conference
   * @param chairId      ID của chair (for authorization)
   * @return WorkloadStatsDTO chứa thông tin statistics
   */
  // Thống kê workload toàn bộ conference
  public WorkloadStatsDTO getConferenceWorkloadStats(Long conferenceId, Long chairId) {
    Conference conference = conferenceRepository
        .findById(conferenceId)
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can view workload statistics");
    }

    // Get all PC members
    List<PCMember> pcMembers = pcMemberRepository.findByConferenceId(conferenceId).stream()
        .filter(member -> member.getStatus() == PCMember.PCMemberStatus.ACCEPTED)
        .collect(Collectors.toList());

    int totalReviewers = pcMembers.size();

    // Calculate statistics
    int totalAssignments = 0;
    int assignedCount = 0;
    int acceptedCount = 0;
    int declinedCount = 0;
    int completedCount = 0;

    int lowWorkloadCount = 0;
    int normalWorkloadCount = 0;
    int highWorkloadCount = 0;
    int overloadedCount = 0;

    // Optimized: Single query to get all assignments for the conference
    List<Assignment> allConferenceAssignments = assignmentRepository.findByConferenceId(conferenceId);

    // Group assignments by reviewerId
    Map<Long, List<Assignment>> reviewerAssignmentsMap = allConferenceAssignments.stream()
        .collect(Collectors.groupingBy(Assignment::getReviewerId));

    // Batch fetch User details
    List<Long> reviewerIds = pcMembers.stream().map(PCMember::getUserId).collect(Collectors.toList());
    Map<Long, User> userMap = userRepository.findAllById(reviewerIds).stream()
        .collect(Collectors.toMap(User::getId, u -> u));

    List<WorkloadDTO> reviewerWorkloads = pcMembers.stream()
        .map(member -> {
          List<Assignment> conferenceAssignments = reviewerAssignmentsMap.getOrDefault(member.getUserId(),
              new ArrayList<>());

          // Count assignments by status for this conference
          long rAssignedCount = conferenceAssignments.stream()
              .filter(a -> a.getStatus() == Assignment.AssignmentStatus.ASSIGNED)
              .count();
          long rAcceptedCount = conferenceAssignments.stream()
              .filter(a -> a.getStatus() == Assignment.AssignmentStatus.ACCEPTED)
              .count();
          long rDeclinedCount = conferenceAssignments.stream()
              .filter(a -> a.getStatus() == Assignment.AssignmentStatus.DECLINED)
              .count();
          long rCompletedCount = conferenceAssignments.stream()
              .filter(a -> a.getStatus() == Assignment.AssignmentStatus.COMPLETED)
              .count();

          long totalCount = rAssignedCount + rAcceptedCount + rCompletedCount; // Exclude declined

          User reviewer = userMap.get(member.getUserId());
          String wStatus = calculateWorkloadStatus(totalCount);
          double wPercentage = (double) totalCount / maxAssignmentsPerReviewer * 100.0;

          return WorkloadDTO.builder()
              .reviewerId(member.getUserId())
              .reviewerEmail(reviewer != null ? reviewer.getEmail() : null)
              .reviewerName(reviewer != null ? reviewer.getFullName() : null)
              .conferenceId(conferenceId)
              .conferenceName(conference.getName())
              .totalAssignments(totalCount)
              .assignedCount(rAssignedCount)
              .acceptedCount(rAcceptedCount)
              .declinedCount(rDeclinedCount)
              .completedCount(rCompletedCount)
              .workloadStatus(wStatus)
              .maxAssignments(maxAssignmentsPerReviewer)
              .workloadPercentage(wPercentage)
              .build();
        })
        .collect(Collectors.toList());

    // Aggregate statistics
    for (WorkloadDTO workload : reviewerWorkloads) {
      totalAssignments += workload.getTotalAssignments();
      assignedCount += workload.getAssignedCount();
      acceptedCount += workload.getAcceptedCount();
      declinedCount += workload.getDeclinedCount();
      completedCount += workload.getCompletedCount();

      switch (workload.getWorkloadStatus()) {
        case "LOW":
          lowWorkloadCount++;
          break;
        case "NORMAL":
          normalWorkloadCount++;
          break;
        case "HIGH":
          highWorkloadCount++;
          break;
        case "OVERLOADED":
          overloadedCount++;
          break;
      }
    }

    double averageAssignmentsPerReviewer = totalReviewers > 0 ? (double) totalAssignments / totalReviewers : 0.0;

    return WorkloadStatsDTO.builder()
        .conferenceId(conferenceId)
        .conferenceName(conference.getName())
        .totalReviewers(totalReviewers)
        .totalAssignments(totalAssignments)
        .averageAssignmentsPerReviewer(averageAssignmentsPerReviewer)
        .lowWorkloadCount(lowWorkloadCount)
        .normalWorkloadCount(normalWorkloadCount)
        .highWorkloadCount(highWorkloadCount)
        .overloadedCount(overloadedCount)
        .assignedCount(assignedCount)
        .acceptedCount(acceptedCount)
        .declinedCount(declinedCount)
        .completedCount(completedCount)
        .reviewerWorkloads(reviewerWorkloads)
        .build();
  }

  /**
   * Kiểm tra reviewer có vượt quá workload limit không trong một conference
   *
   * @param reviewerId   ID của reviewer
   * @param conferenceId ID của conference
   * @return true nếu vượt quá limit, false nếu không
   */
  public boolean isOverloaded(Long reviewerId, Long conferenceId) {
    List<Assignment> conferenceAssignments = assignmentRepository.findByReviewerIdAndConferenceId(reviewerId,
        conferenceId);

    long acceptedCount = conferenceAssignments.stream()
        .filter(a -> a.getStatus() == Assignment.AssignmentStatus.ACCEPTED)
        .count();
    long assignedCount = conferenceAssignments.stream()
        .filter(a -> a.getStatus() == Assignment.AssignmentStatus.ASSIGNED)
        .count();
    long total = acceptedCount + assignedCount;
    return total >= maxAssignmentsPerReviewer;
  }

  /**
   * Kiểm tra reviewer có gần đạt workload limit không (warning threshold) trong
   * một conference
   *
   * @param reviewerId   ID của reviewer
   * @param conferenceId ID của conference
   * @return true nếu gần đạt limit, false nếu không
   */
  public boolean isNearLimit(Long reviewerId, Long conferenceId) {
    List<Assignment> conferenceAssignments = assignmentRepository.findByReviewerIdAndConferenceId(reviewerId,
        conferenceId);

    long acceptedCount = conferenceAssignments.stream()
        .filter(a -> a.getStatus() == Assignment.AssignmentStatus.ACCEPTED)
        .count();
    long assignedCount = conferenceAssignments.stream()
        .filter(a -> a.getStatus() == Assignment.AssignmentStatus.ASSIGNED)
        .count();
    long total = acceptedCount + assignedCount;
    double percentage = (double) total / maxAssignmentsPerReviewer;
    return percentage >= warningThreshold && percentage < 1.0;
  }

  /**
   * Tính toán workload status dựa trên số lượng assignments
   *
   * @param totalAssignments Tổng số assignments
   * @return Workload status (LOW, NORMAL, HIGH, OVERLOADED)
   */
  /**
   * Tính toán workload status dựa trên số lượng assignments
   *
   * @param totalAssignments Tổng số assignments
   * @return Workload status (LOW, NORMAL, HIGH, OVERLOADED)
   */
  // Tính trạng thái tải (Thấp/Bình thường/Cao/Quá tải)
  public String calculateWorkloadStatus(long totalAssignments) {
    if (totalAssignments >= maxAssignmentsPerReviewer) {
      return "OVERLOADED";
    } else if (totalAssignments >= maxAssignmentsPerReviewer * 0.75) {
      return "HIGH";
    } else if (totalAssignments >= maxAssignmentsPerReviewer * 0.5) {
      return "NORMAL";
    } else {
      return "LOW";
    }
  }

  /**
   * Lấy max assignments per reviewer (configurable)
   *
   * @return Max assignments per reviewer
   */
  public int getMaxAssignmentsPerReviewer() {
    return maxAssignmentsPerReviewer;
  }

  /**
   * Lấy workload alerts (overloaded và near-limit reviewers) cho một conference
   *
   * @param conferenceId ID của conference
   * @param chairId      ID của chair (for authorization)
   * @return List of WorkloadAlertDTO
   */
  /**
   * Lấy workload alerts (overloaded và near-limit reviewers) cho một conference
   *
   * @param conferenceId ID của conference
   * @param chairId      ID của chair (for authorization)
   * @return List of WorkloadAlertDTO
   */
  // Lấy danh sách cảnh báo quá tải
  public List<com.uth.confms.pc.dto.WorkloadAlertDTO> getWorkloadAlerts(
      Long conferenceId, Long chairId) {
    Conference conference = conferenceRepository
        .findById(conferenceId)
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new com.uth.confms.common.exception.UnauthorizedException(
          "Only conference chair can view workload alerts");
    }

    WorkloadStatsDTO stats = getConferenceWorkloadStats(conferenceId, chairId);

    return stats.getReviewerWorkloads().stream()
        .map(
            workload -> {
              // Check if overloaded or near limit
              String alertType = null;
              String message = null;

              if (workload.getWorkloadStatus().equals("OVERLOADED")) {
                alertType = "OVERLOADED";
                message = String.format(
                    "Reviewer has exceeded the maximum workload limit (%d assignments)",
                    maxAssignmentsPerReviewer);
              } else if (workload.getWorkloadPercentage() >= warningThreshold * 100.0) {
                alertType = "NEAR_LIMIT";
                message = String.format(
                    "Reviewer is near the workload limit (%d/%d assignments, %.1f%%)",
                    workload.getTotalAssignments(),
                    maxAssignmentsPerReviewer,
                    workload.getWorkloadPercentage());
              }

              // Only return alerts
              if (alertType == null) {
                return null;
              }

              return com.uth.confms.pc.dto.WorkloadAlertDTO.builder()
                  .reviewerId(workload.getReviewerId())
                  .reviewerEmail(workload.getReviewerEmail())
                  .reviewerName(workload.getReviewerName())
                  .conferenceId(conferenceId)
                  .conferenceName(workload.getConferenceName())
                  .currentAssignments((int) workload.getTotalAssignments())
                  .maxAssignments(maxAssignmentsPerReviewer)
                  .workloadPercentage(workload.getWorkloadPercentage())
                  .alertType(alertType)
                  .message(message)
                  .build();
            })
        .filter(alert -> alert != null)
        .collect(Collectors.toList());
  }
}
