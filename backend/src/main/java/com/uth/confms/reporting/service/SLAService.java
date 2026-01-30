package com.uth.confms.reporting.service;

import com.uth.confms.assignment.entity.Assignment;
import com.uth.confms.assignment.repository.AssignmentRepository;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.conference.entity.Deadline;
import com.uth.confms.conference.repository.DeadlineRepository;
import com.uth.confms.reporting.dto.SLAStatsDTO;
import com.uth.confms.review.entity.Review;
import com.uth.confms.review.repository.ReviewRepository;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service để tính toán SLA (Service Level Agreement) metrics cho reviews
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SLAService {

    private final SubmissionRepository submissionRepository;
    private final ReviewRepository reviewRepository;
    private final AssignmentRepository assignmentRepository;
    private final DeadlineRepository deadlineRepository;
    private final UserRepository userRepository;

    /**
     * Get SLA statistics cho một conference
     */
    @Transactional(readOnly = true)
    // Tính toán và lấy thống kê SLA (Service Level Agreement)
    public SLAStatsDTO getSLAStats(Long conferenceId) {
        log.info("Calculating SLA stats for conference {}", conferenceId);

        // Get review deadline
        List<Deadline> deadlines = deadlineRepository.findByConferenceId(conferenceId);
        Deadline reviewDeadline = deadlines.stream()
                .filter(d -> d.getType() == Deadline.DeadlineType.REVIEW)
                .findFirst()
                .orElse(null);

        LocalDateTime deadlineDate = reviewDeadline != null ? reviewDeadline.getDueDate() : null;
        boolean hasDeadline = deadlineDate != null;

        // Get all submissions for this conference
        List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
        List<Long> submissionIds = submissions.stream()
                .map(Submission::getId)
                .collect(Collectors.toList());

        // Get all assignments
        List<Assignment> allAssignments = new ArrayList<>();
        for (Long submissionId : submissionIds) {
            allAssignments.addAll(assignmentRepository.findBySubmissionId(submissionId));
        }

        // Filter accepted assignments (only these count for SLA)
        List<Assignment> acceptedAssignments = allAssignments.stream()
                .filter(a -> a.getStatus() == Assignment.AssignmentStatus.ACCEPTED
                        || a.getStatus() == Assignment.AssignmentStatus.COMPLETED)
                .collect(Collectors.toList());

        int totalAssignments = acceptedAssignments.size();
        int completedOnTime = 0;
        int completedLate = 0;
        int pendingPastDeadline = 0;
        List<SLAStatsDTO.ViolationDTO> violations = new ArrayList<>();

        // Group by reviewer for per-reviewer stats
        Map<Long, List<Assignment>> assignmentsByReviewer = acceptedAssignments.stream()
                .collect(Collectors.groupingBy(Assignment::getReviewerId));

        List<SLAStatsDTO.ReviewerSLADTO> reviewerSLAs = new ArrayList<>();

        for (Map.Entry<Long, List<Assignment>> entry : assignmentsByReviewer.entrySet()) {
            Long reviewerId = entry.getKey();
            List<Assignment> reviewerAssignments = entry.getValue();

            User reviewer = userRepository.findById(reviewerId)
                    .orElse(null);
            String reviewerName = reviewer != null ? reviewer.getFullName() : "Unknown";

            int reviewerOnTime = 0;
            int reviewerLate = 0;

            for (Assignment assignment : reviewerAssignments) {
                // Get review for this assignment
                List<Review> reviews = reviewRepository.findByAssignmentId(assignment.getId());
                Review review = reviews.isEmpty() ? null : reviews.get(0);

                if (review != null && review.getStatus() == Review.ReviewStatus.SUBMITTED) {
                    // Review completed
                    if (hasDeadline && review.getSubmittedAt() != null) {
                        if (review.getSubmittedAt().isBefore(deadlineDate)
                                || review.getSubmittedAt().isEqual(deadlineDate)) {
                            completedOnTime++;
                            reviewerOnTime++;
                        } else {
                            completedLate++;
                            reviewerLate++;

                            // Add to violations
                            long daysLate = Duration.between(deadlineDate, review.getSubmittedAt()).toDays();
                            violations.add(SLAStatsDTO.ViolationDTO.builder()
                                    .assignmentId(assignment.getId())
                                    .submissionId(assignment.getSubmissionId())
                                    .reviewerId(reviewerId)
                                    .reviewerName(reviewerName)
                                    .deadline(deadlineDate)
                                    .submittedAt(review.getSubmittedAt())
                                    .daysLate(daysLate)
                                    .build());
                        }
                    } else {
                        // No deadline, consider as on-time
                        completedOnTime++;
                        reviewerOnTime++;
                    }
                } else {
                    // Review not completed
                    if (hasDeadline && LocalDateTime.now().isAfter(deadlineDate)) {
                        pendingPastDeadline++;
                    }
                }
            }

            // Calculate reviewer SLA
            int reviewerTotal = reviewerAssignments.size();
            double reviewerOnTimeRate = reviewerTotal > 0
                    ? (double) reviewerOnTime / reviewerTotal * 100.0
                    : 0.0;

            reviewerSLAs.add(SLAStatsDTO.ReviewerSLADTO.builder()
                    .reviewerId(reviewerId)
                    .reviewerName(reviewerName)
                    .totalAssignments(reviewerTotal)
                    .onTimeCount(reviewerOnTime)
                    .lateCount(reviewerLate)
                    .onTimeRate(reviewerOnTimeRate)
                    .build());
        }

        // Calculate overall on-time completion rate
        int completedTotal = completedOnTime + completedLate;
        double onTimeCompletionRate = completedTotal > 0
                ? (double) completedOnTime / completedTotal * 100.0
                : 0.0;

        return SLAStatsDTO.builder()
                .conferenceId(conferenceId)
                .totalAssignments(totalAssignments)
                .completedOnTime(completedOnTime)
                .completedLate(completedLate)
                .pendingPastDeadline(pendingPastDeadline)
                .onTimeCompletionRate(onTimeCompletionRate)
                .reviewDeadline(deadlineDate)
                .hasDeadline(hasDeadline)
                .violations(violations)
                .reviewerSLAs(reviewerSLAs)
                .build();
    }
}
