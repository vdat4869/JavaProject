package com.uth.confms.pc.service;

import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.pc.entity.ConflictOfInterest;
import com.uth.confms.pc.repository.ConflictOfInterestRepository;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.entity.SubmissionAuthor;
import com.uth.confms.submission.repository.SubmissionAuthorRepository;
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
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class COIService {

  private final ConflictOfInterestRepository coiRepository;
  private final SubmissionRepository submissionRepository;
  private final UserRepository userRepository;
  private final SubmissionAuthorRepository submissionAuthorRepository;

  // Kiểm tra xem reviewer có COI với submission không
  public boolean hasCOI(Long reviewerId, Long submissionId) {
    return coiRepository.findByReviewerIdAndSubmissionId(reviewerId, submissionId)
        .map(ConflictOfInterest::getActive)
        .orElse(false);
  }

  @Transactional
  // Tự động phát hiện và gợi ý COI cho reviewer cụ thể
  public void detectAndSuggestCOI(Long reviewerId, Long submissionId) {
    Submission submission = submissionRepository.findById(submissionId)
        .orElseThrow(() -> new RuntimeException("Submission not found"));

    // Check if reviewer is the author (primary author)
    if (submission.getAuthorId().equals(reviewerId)) {
      saveInstitutionalCOI(reviewerId, submissionId, "Reviewer is the primary author of this submission");
      return;
    }

    // Check if reviewer is any of the co-authors
    List<SubmissionAuthor> authors = submissionAuthorRepository.findBySubmission(submission);
    for (SubmissionAuthor author : authors) {
      if (author.getUserId() != null && author.getUserId().equals(reviewerId)) {
        saveInstitutionalCOI(reviewerId, submissionId, "Reviewer is a co-author of this submission");
        return;
      }
    }

    // Automatic institutional COI detection
    User reviewer = userRepository.findById(reviewerId).orElseThrow();
    if (reviewer.getOrganization() == null) {
      return;
    }

    Long reviewerOrgId = reviewer.getOrganization().getId();

    // Check primary author's organization
    User primaryAuthor = userRepository.findById(submission.getAuthorId()).orElseThrow();
    if (primaryAuthor.getOrganization() != null
        && primaryAuthor.getOrganization().getId().equals(reviewerOrgId)) {
      saveInstitutionalCOI(reviewerId, submissionId,
          "Same organization as primary author: " + reviewer.getOrganization().getName());
      return;
    }

    // Check all co-authors' organizations
    for (SubmissionAuthor author : authors) {
      if (author.getOrganization() != null && author.getOrganization().getId().equals(reviewerOrgId)) {
        saveInstitutionalCOI(reviewerId, submissionId,
            "Same organization as co-author (" + author.getFirstName() + " " + author.getLastName() + "): "
                + reviewer.getOrganization().getName());
        return;
      }
    }
  }

  @Transactional
  // Quét toàn bộ PC members để phát hiện institutional conflicts
  public void detectInstitutionalConflicts(Long submissionId) {
    Submission submission = submissionRepository.findById(submissionId)
        .orElseThrow(() -> new RuntimeException("Submission not found"));

    // Get all authors (primary + co-authors)
    List<Long> authorUserIds = new ArrayList<>();
    authorUserIds.add(submission.getAuthorId());

    List<SubmissionAuthor> coAuthors = submissionAuthorRepository.findBySubmission(submission);
    for (SubmissionAuthor sa : coAuthors) {
      if (sa.getUserId() != null) {
        authorUserIds.add(sa.getUserId());
      }
    }

    // Get all unique organizations of these authors
    Set<Long> authorOrgIds = new HashSet<>();
    for (Long userId : authorUserIds) {
      userRepository.findById(userId).ifPresent(u -> {
        if (u.getOrganization() != null) {
          authorOrgIds.add(u.getOrganization().getId());
        }
      });
    }

    // Also check organizations directly from coAuthors table (if userId is null but
    // organization is set)
    for (SubmissionAuthor sa : coAuthors) {
      if (sa.getOrganization() != null) {
        authorOrgIds.add(sa.getOrganization().getId());
      }
    }

    if (authorOrgIds.isEmpty()) {
      log.debug("No organizations found for authors of submission {}, skipping institutional COI scan.", submissionId);
      return;
    }

    // Find all users in these organizations
    List<User> usersInSameOrgs = userRepository.findAll().stream()
        .filter(u -> u.getOrganization() != null && authorOrgIds.contains(u.getOrganization().getId()))
        .toList();

    for (User reviewer : usersInSameOrgs) {
      // Don't flag authors as having COI with themselves (handled by other logic)
      if (authorUserIds.contains(reviewer.getId())) {
        continue;
      }

      String orgName = reviewer.getOrganization().getName();
      saveInstitutionalCOI(reviewer.getId(), submissionId,
          "Institutional conflict: Same organization (" + orgName + ")");
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
  // Reviewer chủ động khai báo COI
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
  // Xóa COI đã khai báo (chỉ reviewer sở hữu mới được xóa)
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

  // Thống kê tình hình COI của conference
  public COIStatisticsDTO getCOIStatistics(Long conferenceId, Long chairId) {
    List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
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
