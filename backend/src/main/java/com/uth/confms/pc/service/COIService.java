package com.uth.confms.pc.service;

import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.pc.entity.ConflictOfInterest;
import com.uth.confms.pc.repository.ConflictOfInterestRepository;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uth.confms.pc.dto.COIDeclareDTO;
import com.uth.confms.pc.dto.COIHistoryDTO;
import com.uth.confms.pc.dto.COIStatisticsDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class COIService {

  private final ConflictOfInterestRepository coiRepository;
  private final SubmissionRepository submissionRepository;
  private final UserRepository userRepository;

  public boolean hasCOI(Long reviewerId, Long submissionId) {
    return coiRepository.findByReviewerIdAndSubmissionId(reviewerId, submissionId)
        .map(ConflictOfInterest::getActive)
        .orElse(false);
  }

  @Transactional
  public void detectAndSuggestCOI(Long reviewerId, Long submissionId) {
    Submission submission = submissionRepository.findById(submissionId)
        .orElseThrow(() -> new RuntimeException("Submission not found"));

    // Check if reviewer is the author
    if (submission.getAuthorId().equals(reviewerId)) {
      saveInstitutionalCOI(reviewerId, submissionId, "Reviewer is the author of this submission");
      return;
    }

    // Automatic institutional COI detection
    User reviewer = userRepository.findById(reviewerId).orElseThrow();
    User author = userRepository.findById(submission.getAuthorId()).orElseThrow();

    if (reviewer.getOrganization() != null && author.getOrganization() != null
        && reviewer.getOrganization().getId().equals(author.getOrganization().getId())) {
      saveInstitutionalCOI(reviewerId, submissionId, "Same organization: " + reviewer.getOrganization().getName());
    }
  }

  @Transactional
  public void detectInstitutionalConflicts(Long submissionId) {
    Submission submission = submissionRepository.findById(submissionId)
        .orElseThrow(() -> new RuntimeException("Submission not found"));

    User author = userRepository.findById(submission.getAuthorId())
        .orElseThrow(() -> new RuntimeException("Author not found"));

    if (author.getOrganization() == null) {
      log.debug("Author has no organization, skipping institutional COI check.");
      return;
    }

    Long orgId = author.getOrganization().getId();

    List<User> usersInSameOrg = userRepository.findAll().stream()
        .filter(u -> u.getOrganization() != null && u.getOrganization().getId().equals(orgId))
        .filter(u -> !u.getId().equals(author.getId()))
        .toList();

    for (User reviewer : usersInSameOrg) {
      saveInstitutionalCOI(reviewer.getId(), submissionId, "Same organization: " + author.getOrganization().getName());
    }
  }

  private void saveInstitutionalCOI(Long reviewerId, Long submissionId, String reason) {
    Optional<ConflictOfInterest> existing = coiRepository.findByReviewerIdAndSubmissionId(reviewerId, submissionId);
    if (existing.isEmpty()) {
      ConflictOfInterest coi = ConflictOfInterest.builder()
          .reviewerId(reviewerId)
          .submissionId(submissionId)
          .type(ConflictOfInterest.COIType.INSTITUTIONAL)
          .reason(reason)
          .active(true)
          .declaredAt(LocalDateTime.now())
          .build();
      coiRepository.save(coi);
      log.info("Saved institutional COI for reviewer {} on submission {}", reviewerId, submissionId);
    }
  }

  @Transactional
  public ConflictOfInterest declareCOI(COIDeclareDTO dto, Long reviewerId, HttpServletRequest request) {
    ConflictOfInterest.COIType type;
    try {
      type = ConflictOfInterest.COIType.valueOf(dto.getType());
    } catch (IllegalArgumentException e) {
      type = ConflictOfInterest.COIType.OTHER;
    }

    ConflictOfInterest coi = ConflictOfInterest.builder()
        .reviewerId(reviewerId)
        .submissionId(dto.getSubmissionId())
        .type(type)
        .reason(dto.getReason())
        .active(true)
        .declaredAt(LocalDateTime.now())
        .build();
    return coiRepository.save(coi);
  }

  @Transactional
  public void removeCOI(Long coiId, Long reviewerId, HttpServletRequest request) {
    ConflictOfInterest coi = coiRepository.findById(coiId)
        .orElseThrow(() -> new RuntimeException("COI not found"));
    if (!coi.getReviewerId().equals(reviewerId)) {
      throw new RuntimeException("Unauthorized");
    }
    coiRepository.delete(coi);
  }

  public List<ConflictOfInterest> getCOIsByReviewer(Long reviewerId) {
    return coiRepository.findByReviewerId(reviewerId);
  }

  public List<ConflictOfInterest> getCOIsBySubmission(Long submissionId) {
    return coiRepository.findBySubmissionId(submissionId);
  }

  public List<COIHistoryDTO> getCOIHistory(Long conferenceId, Long chairId) {
    // Placeholder implementation
    return new ArrayList<>();
  }

  public COIStatisticsDTO getCOIStatistics(Long conferenceId, Long chairId) {
    List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
    long totalSubmissions = submissions.size();
    long submissionsWithCOI = submissions.stream()
        .filter(s -> !coiRepository.findBySubmissionId(s.getId()).isEmpty())
        .count();

    return COIStatisticsDTO.builder()
        .conferenceId(conferenceId)
        .submissionsWithCOIs((int) submissionsWithCOI)
        .coiByType(new HashMap<>())
        .build();
  }
}
