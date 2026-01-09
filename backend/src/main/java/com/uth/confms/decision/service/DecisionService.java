package com.uth.confms.decision.service;

import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.decision.dto.DecisionRequestDTO;
import com.uth.confms.decision.dto.DecisionResultDTO;
import com.uth.confms.decision.entity.Decision;
import com.uth.confms.decision.repository.DecisionRepository;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.repository.SubmissionRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý decisions (quyết định Accept/Reject)
 *
 * <p>Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 *   <li>Tạo decision cho submission (chỉ chair)
 *   <li>Auto-update submission status
 *   <li>Quản lý pending notifications
 *   <li>Validation: submission phải ở status UNDER_REVIEW
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class DecisionService {
  private final DecisionRepository decisionRepository;
  private final SubmissionRepository submissionRepository;
  private final ConferenceRepository conferenceRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  public DecisionService(
      DecisionRepository decisionRepository,
      SubmissionRepository submissionRepository,
      ConferenceRepository conferenceRepository,
      UserRepository userRepository,
      NotificationService notificationService) {
    this.decisionRepository = decisionRepository;
    this.submissionRepository = submissionRepository;
    this.conferenceRepository = conferenceRepository;
    this.userRepository = userRepository;
    this.notificationService = notificationService;
  }

  @Transactional
  public DecisionResultDTO makeDecision(DecisionRequestDTO dto, Long chairId) {
    Submission submission =
        submissionRepository
            .findById(dto.getSubmissionId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Submission with id " + dto.getSubmissionId() + " not found"));

    Conference conference =
        conferenceRepository
            .findById(submission.getConferenceId())
            .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization - only chair can make decisions
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can make decisions");
    }

    // Check if submission is in review
    if (submission.getStatus() != Submission.SubmissionStatus.UNDER_REVIEW) {
      throw new BusinessException("Submission must be under review before making decision");
    }

    // Check if decision already exists
    decisionRepository
        .findBySubmissionId(dto.getSubmissionId())
        .ifPresent(
            existing -> {
              throw new BusinessException("Decision already exists for this submission");
            });

    // Validate decision type
    Decision.DecisionType decisionType;
    try {
      decisionType = Decision.DecisionType.valueOf(dto.getType());
    } catch (IllegalArgumentException e) {
      throw new BusinessException("Invalid decision type: " + dto.getType());
    }

    // Create decision
    Decision decision =
        Decision.builder()
            .submissionId(dto.getSubmissionId())
            .decidedBy(chairId)
            .type(decisionType)
            .comments(dto.getComments())
            .notified(false)
            .build();

    decision = decisionRepository.save(decision);

    // Update submission status
    if (decisionType == Decision.DecisionType.ACCEPT
        || decisionType == Decision.DecisionType.CONDITIONAL_ACCEPT) {
      submission.setStatus(Submission.SubmissionStatus.ACCEPTED);
    } else {
      submission.setStatus(Submission.SubmissionStatus.REJECTED);
    }
    submissionRepository.save(submission);

    // Send notification if requested
    if (dto.getSendNotification() != null && dto.getSendNotification()) {
      notificationService.sendDecisionNotification(decision);
    }

    return mapToDTO(decision);
  }

  public DecisionResultDTO getDecisionBySubmission(Long submissionId) {
    Decision decision =
        decisionRepository
            .findBySubmissionId(submissionId)
            .orElseThrow(() -> new NotFoundException("Decision not found for this submission"));

    return mapToDTO(decision);
  }

  public List<DecisionResultDTO> getDecisionsByConference(Long conferenceId, Long chairId) {
    Conference conference =
        conferenceRepository
            .findById(conferenceId)
            .orElseThrow(() -> new NotFoundException("Conference not found"));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can view decisions");
    }

    // Get all submissions for this conference
    List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
    List<Long> submissionIds =
        submissions.stream().map(Submission::getId).collect(Collectors.toList());

    // Get decisions for these submissions
    return submissionIds.stream()
        .map(submissionId -> decisionRepository.findBySubmissionId(submissionId).orElse(null))
        .filter(decision -> decision != null)
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  public List<DecisionResultDTO> getPendingNotifications() {
    return decisionRepository.findByNotifiedFalse().stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  public Decision getDecisionEntityById(Long decisionId) {
    return decisionRepository
        .findById(decisionId)
        .orElseThrow(() -> new NotFoundException("Decision not found"));
  }

  private DecisionResultDTO mapToDTO(Decision decision) {
    Submission submission = submissionRepository.findById(decision.getSubmissionId()).orElse(null);
    User decidedByUser = userRepository.findById(decision.getDecidedBy()).orElse(null);

    return DecisionResultDTO.builder()
        .id(decision.getId())
        .submissionId(decision.getSubmissionId())
        .submissionTitle(submission != null ? submission.getTitle() : null)
        .decidedBy(decision.getDecidedBy())
        .decidedByName(decidedByUser != null ? decidedByUser.getFullName() : null)
        .type(decision.getType().name())
        .comments(decision.getComments())
        .notified(decision.getNotified())
        .decidedAt(decision.getDecidedAt())
        .build();
  }
}
