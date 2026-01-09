package com.uth.confms.reporting.service;

import com.uth.confms.reporting.dto.ConferenceStatsDTO;
import com.uth.confms.reporting.dto.ReviewStatsDTO;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.repository.SubmissionRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
  private final SubmissionRepository submissionRepository;

  public ReportService(SubmissionRepository submissionRepository) {
    this.submissionRepository = submissionRepository;
  }

  public ConferenceStatsDTO getConferenceStats(Long conferenceId) {
    List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);

    int total = submissions.size();
    long accepted =
        submissions.stream()
            .filter(s -> s.getStatus() == Submission.SubmissionStatus.ACCEPTED)
            .count();
    long rejected =
        submissions.stream()
            .filter(s -> s.getStatus() == Submission.SubmissionStatus.REJECTED)
            .count();
    long pending =
        submissions.stream()
            .filter(
                s ->
                    s.getStatus() == Submission.SubmissionStatus.UNDER_REVIEW
                        || s.getStatus() == Submission.SubmissionStatus.SUBMITTED)
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

  public ReviewStatsDTO getReviewStats(Long conferenceId) {
    // Implementation for review statistics
    return ReviewStatsDTO.builder().conferenceId(conferenceId).build();
  }
}
