package com.uth.confms.reporting.service;

import com.uth.confms.assignment.entity.Assignment;
import com.uth.confms.assignment.repository.AssignmentRepository;
import com.uth.confms.reporting.dto.ConferenceStatsDTO;
import com.uth.confms.reporting.dto.ReviewStatsDTO;
import com.uth.confms.review.entity.Review;
import com.uth.confms.review.repository.ReviewRepository;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.repository.SubmissionRepository;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
  private final SubmissionRepository submissionRepository;
  private final ReviewRepository reviewRepository;
  private final AssignmentRepository assignmentRepository;

  public ReportService(
      SubmissionRepository submissionRepository,
      ReviewRepository reviewRepository,
      AssignmentRepository assignmentRepository) {
    this.submissionRepository = submissionRepository;
    this.reviewRepository = reviewRepository;
    this.assignmentRepository = assignmentRepository;
  }

  // Lấy thống kê chung của conference (submissions, acceptance rate)
  public ConferenceStatsDTO getConferenceStats(Long conferenceId) {
    List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);

    int total = submissions.size();
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

    double acceptanceRate = total > 0 ? (double) accepted / total * 100 : 0.0;

    return ConferenceStatsDTO.builder()
        .conferenceId(conferenceId)
        .totalSubmissions(total)
        .acceptedCount((int) accepted)
        .rejectedCount((int) rejected)
        .pendingCount((int) pending)
        .acceptanceRate(acceptanceRate)
        .build();
  }

  // Lấy thống kê chi tiết về review và assignments
  public ReviewStatsDTO getReviewStats(Long conferenceId) {
    // Get all submissions for this conference
    List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
    List<Long> submissionIds = submissions.stream()
        .map(Submission::getId)
        .collect(Collectors.toList());

    // Calculate assignment statistics
    int totalAssignments = 0;
    int completedReviews = 0;
    int pendingReviews = 0;
    long totalReviewTimeHours = 0;
    int reviewsWithTime = 0;

    for (Long submissionId : submissionIds) {
      // Get assignments for this submission
      List<Assignment> assignments = assignmentRepository.findBySubmissionId(submissionId);
      totalAssignments += assignments.size();

      // Get reviews for this submission
      List<Review> reviews = reviewRepository.findBySubmissionId(submissionId);

      for (Review review : reviews) {
        if (review.getStatus() == Review.ReviewStatus.SUBMITTED) {
          completedReviews++;

          // Calculate review time if both timestamps are available
          if (review.getCreatedAt() != null && review.getSubmittedAt() != null) {
            Duration duration = Duration.between(review.getCreatedAt(), review.getSubmittedAt());
            totalReviewTimeHours += duration.toHours();
            reviewsWithTime++;
          }
        } else if (review.getStatus() == Review.ReviewStatus.DRAFT) {
          pendingReviews++;
        }
      }
    }

    // Calculate completion rate
    double completionRate = totalAssignments > 0
        ? (double) completedReviews / totalAssignments * 100.0
        : 0.0;

    // Calculate average review time
    Integer averageReviewTime = reviewsWithTime > 0
        ? (int) (totalReviewTimeHours / reviewsWithTime)
        : null;

    return ReviewStatsDTO.builder()
        .conferenceId(conferenceId)
        .totalAssignments(totalAssignments)
        .completedReviews(completedReviews)
        .pendingReviews(pendingReviews)
        .completionRate(completionRate)
        .averageReviewTime(averageReviewTime)
        .build();
  }
}
