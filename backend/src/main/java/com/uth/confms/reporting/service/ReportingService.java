package com.uth.confms.reporting.service;

import com.uth.confms.assignment.entity.Assignment;
import com.uth.confms.assignment.repository.AssignmentRepository;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.reporting.dto.ReportResponseDTO;
import com.uth.confms.reporting.entity.ReportSnapshot;
import com.uth.confms.reporting.repository.ReportRepository;
import com.uth.confms.review.entity.Review;
import com.uth.confms.review.repository.ReviewRepository;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.repository.SubmissionRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service tạo và quản lý reports (báo cáo thống kê)
 *
 * <p>
 * Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 * <li>Generate reports on-the-fly từ data
 * <li>Tạo và lưu report snapshots
 * <li>Report history tracking
 * <li>Statistics: submissions, reviews, assignments
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class ReportingService {
  private final ReportRepository reportRepository;
  private final SubmissionRepository submissionRepository;
  private final ReviewRepository reviewRepository;
  private final AssignmentRepository assignmentRepository;
  private final ConferenceRepository conferenceRepository;

  public ReportingService(
      ReportRepository reportRepository,
      SubmissionRepository submissionRepository,
      ReviewRepository reviewRepository,
      AssignmentRepository assignmentRepository,
      ConferenceRepository conferenceRepository) {
    this.reportRepository = reportRepository;
    this.submissionRepository = submissionRepository;
    this.reviewRepository = reviewRepository;
    this.assignmentRepository = assignmentRepository;
    this.conferenceRepository = conferenceRepository;
  }

  // Tạo báo cáo thống kê mới nhất từ dữ liệu thực
  public ReportResponseDTO generateReport(Long conferenceId, Long chairId) {
    Conference conference = conferenceRepository
        .findById(conferenceId)
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can generate reports");
    }

    // Get all submissions for this conference
    List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);

    // Calculate submission statistics
    int totalSubmissions = submissions.size();
    long accepted = submissions.stream()
        .filter(s -> s.getStatus() == Submission.SubmissionStatus.ACCEPTED)
        .count();
    long rejected = submissions.stream()
        .filter(s -> s.getStatus() == Submission.SubmissionStatus.REJECTED)
        .count();
    long pending = submissions.stream()
        .filter(
            s -> s.getStatus() == Submission.SubmissionStatus.UNDER_REVIEW
                || s.getStatus() == Submission.SubmissionStatus.SUBMITTED
                || s.getStatus() == Submission.SubmissionStatus.REVIEWED)
        .count();

    double acceptanceRate = totalSubmissions > 0 ? (double) accepted / totalSubmissions * 100 : 0.0;

    // Get all submission IDs
    List<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());

    // Calculate review statistics
    int totalReviews = 0;
    int completedReviews = 0;
    int pendingReviews = 0;

    for (Long submissionId : submissionIds) {
      List<Review> reviews = reviewRepository.findBySubmissionId(submissionId);
      totalReviews += reviews.size();
      completedReviews += (int) reviews.stream().filter(r -> r.getStatus() == Review.ReviewStatus.SUBMITTED).count();
      pendingReviews += (int) reviews.stream().filter(r -> r.getStatus() == Review.ReviewStatus.DRAFT).count();
    }

    // Calculate assignment statistics
    int totalAssignments = 0;
    int acceptedAssignments = 0;
    int declinedAssignments = 0;

    for (Long submissionId : submissionIds) {
      List<Assignment> assignments = assignmentRepository.findBySubmissionId(submissionId);
      totalAssignments += assignments.size();
      acceptedAssignments += (int) assignments.stream()
          .filter(a -> a.getStatus() == Assignment.AssignmentStatus.ACCEPTED)
          .count();
      declinedAssignments += (int) assignments.stream()
          .filter(a -> a.getStatus() == Assignment.AssignmentStatus.DECLINED)
          .count();
    }

    return ReportResponseDTO.builder()
        .conferenceId(conferenceId)
        .totalSubmissions(totalSubmissions)
        .acceptedCount((int) accepted)
        .rejectedCount((int) rejected)
        .pendingCount((int) pending)
        .acceptanceRate(acceptanceRate)
        .totalReviews(totalReviews)
        .completedReviews(completedReviews)
        .pendingReviews(pendingReviews)
        .totalAssignments(totalAssignments)
        .acceptedAssignments(acceptedAssignments)
        .declinedAssignments(declinedAssignments)
        .build();
  }

  @Transactional
  // Tạo và lưu snapshot báo cáo hiện tại
  public ReportResponseDTO createSnapshot(Long conferenceId, Long chairId) {
    ReportResponseDTO report = generateReport(conferenceId, chairId);

    ReportSnapshot snapshot = ReportSnapshot.builder()
        .conferenceId(report.getConferenceId())
        .totalSubmissions(report.getTotalSubmissions())
        .acceptedCount(report.getAcceptedCount())
        .rejectedCount(report.getRejectedCount())
        .pendingCount(report.getPendingCount())
        .acceptanceRate(report.getAcceptanceRate())
        .totalReviews(report.getTotalReviews())
        .completedReviews(report.getCompletedReviews())
        .pendingReviews(report.getPendingReviews())
        .totalAssignments(report.getTotalAssignments())
        .acceptedAssignments(report.getAcceptedAssignments())
        .declinedAssignments(report.getDeclinedAssignments())
        .build();

    snapshot = reportRepository.save(snapshot);

    return mapToDTO(snapshot);
  }

  // Lấy báo cáo mới nhất (ưu tiên snapshot gần nhất, nếu không có thì generate
  // mới)
  public ReportResponseDTO getLatestReport(Long conferenceId, Long chairId) {
    Conference conference = conferenceRepository
        .findById(conferenceId)
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can view reports");
    }

    // Try to get latest snapshot
    ReportSnapshot snapshot = reportRepository.findFirstByConferenceIdOrderBySnapshotAtDesc(conferenceId).orElse(null);

    if (snapshot != null) {
      return mapToDTO(snapshot);
    }

    // If no snapshot exists, generate on-the-fly
    return generateReport(conferenceId, chairId);
  }

  // Lấy lịch sử các snapshots báo cáo
  public List<ReportResponseDTO> getReportHistory(Long conferenceId, Long chairId) {
    Conference conference = conferenceRepository
        .findById(conferenceId)
        .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can view report history");
    }

    return reportRepository.findByConferenceIdOrderBySnapshotAtDesc(conferenceId).stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  private ReportResponseDTO mapToDTO(ReportSnapshot snapshot) {
    return ReportResponseDTO.builder()
        .id(snapshot.getId())
        .conferenceId(snapshot.getConferenceId())
        .totalSubmissions(snapshot.getTotalSubmissions())
        .acceptedCount(snapshot.getAcceptedCount())
        .rejectedCount(snapshot.getRejectedCount())
        .pendingCount(snapshot.getPendingCount())
        .acceptanceRate(snapshot.getAcceptanceRate())
        .totalReviews(snapshot.getTotalReviews())
        .completedReviews(snapshot.getCompletedReviews())
        .pendingReviews(snapshot.getPendingReviews())
        .totalAssignments(snapshot.getTotalAssignments())
        .acceptedAssignments(snapshot.getAcceptedAssignments())
        .declinedAssignments(snapshot.getDeclinedAssignments())
        .snapshotAt(snapshot.getSnapshotAt())
        .build();
  }
}
