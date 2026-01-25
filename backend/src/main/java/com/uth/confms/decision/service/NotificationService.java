package com.uth.confms.decision.service;

import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.decision.dto.BulkNotificationRequestDTO;
import com.uth.confms.decision.entity.Decision;
import com.uth.confms.decision.entity.DecisionHistory;
import com.uth.confms.decision.entity.NotificationLog;
import com.uth.confms.decision.repository.DecisionHistoryRepository;
import com.uth.confms.decision.repository.DecisionRepository;
import com.uth.confms.decision.repository.NotificationLogRepository;
import com.uth.confms.email.service.EmailService;
import com.uth.confms.review.entity.Review;
import com.uth.confms.review.repository.ReviewRepository;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.entity.SubmissionAuthor;
import com.uth.confms.submission.repository.SubmissionAuthorRepository;
import com.uth.confms.submission.repository.SubmissionRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class NotificationService {
  private final NotificationLogRepository notificationLogRepository;
  private final DecisionRepository decisionRepository;
  private final DecisionHistoryRepository decisionHistoryRepository;
  private final SubmissionRepository submissionRepository;
  private final SubmissionAuthorRepository submissionAuthorRepository;
  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;
  private final ConferenceRepository conferenceRepository;
  private final EmailService emailService;

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  public NotificationService(
      NotificationLogRepository notificationLogRepository,
      DecisionRepository decisionRepository,
      DecisionHistoryRepository decisionHistoryRepository,
      SubmissionRepository submissionRepository,
      SubmissionAuthorRepository submissionAuthorRepository,
      ReviewRepository reviewRepository,
      UserRepository userRepository,
      ConferenceRepository conferenceRepository,
      EmailService emailService) {
    this.notificationLogRepository = notificationLogRepository;
    this.decisionRepository = decisionRepository;
    this.decisionHistoryRepository = decisionHistoryRepository;
    this.submissionRepository = submissionRepository;
    this.submissionAuthorRepository = submissionAuthorRepository;
    this.reviewRepository = reviewRepository;
    this.userRepository = userRepository;
    this.conferenceRepository = conferenceRepository;
    this.emailService = emailService;
  }

  @Transactional
  public void sendDecisionNotification(Decision decision) {
    Submission submission =
        submissionRepository
            .findById(decision.getSubmissionId())
            .orElseThrow(() -> new NotFoundException("Submission not found"));

    Conference conference =
        conferenceRepository.findById(submission.getConferenceId()).orElse(null);
    String conferenceName = conference != null ? conference.getName() : "Conference";

    // Get all authors of the submission
    List<SubmissionAuthor> authors =
        submissionAuthorRepository.findBySubmissionId(decision.getSubmissionId());
    Set<Long> authorIds =
        authors.stream().map(SubmissionAuthor::getUserId).collect(Collectors.toSet());

    // Get anonymized feedback from reviews
    List<Review> reviews =
        reviewRepository.findBySubmissionIdAndStatus(
            decision.getSubmissionId(), Review.ReviewStatus.SUBMITTED);

    String anonymizedFeedback = generateAnonymizedFeedback(reviews);

    // Send email to each author using EmailService with templates
    for (Long authorId : authorIds) {
      User author = userRepository.findById(authorId).orElse(null);
      if (author != null && author.getEmail() != null) {
        try {
          sendDecisionEmailWithTemplate(
              author, submission, decision, conferenceName, anonymizedFeedback);

          // Log notification
          NotificationLog.NotificationType notificationType =
              mapDecisionTypeToNotificationType(decision.getType());
          NotificationLog log =
              NotificationLog.builder()
                  .submissionId(decision.getSubmissionId())
                  .userId(authorId)
                  .type(notificationType)
                  .subject(generateEmailSubject(decision, submission.getTitle()))
                  .content(
                      generateEmailContent(decision, submission.getTitle(), anonymizedFeedback))
                  .status(NotificationLog.NotificationStatus.SENT)
                  .build();
          notificationLogRepository.save(log);
        } catch (Exception e) {
          // Log failed notification
          NotificationLog.NotificationType notificationType =
              mapDecisionTypeToNotificationType(decision.getType());
          NotificationLog log =
              NotificationLog.builder()
                  .submissionId(decision.getSubmissionId())
                  .userId(authorId)
                  .type(notificationType)
                  .subject(generateEmailSubject(decision, submission.getTitle()))
                  .content(
                      generateEmailContent(decision, submission.getTitle(), anonymizedFeedback))
                  .status(NotificationLog.NotificationStatus.FAILED)
                  .build();
          notificationLogRepository.save(log);
        }
      }
    }

    // Mark decision as notified and lock it
    boolean wasNotified = decision.getNotified() != null && decision.getNotified();
    boolean wasLocked = decision.getLocked() != null && decision.getLocked();
    
    decision.setNotified(true);
    decision.setLocked(true);
    decisionRepository.save(decision);

    // Log notification status change
    if (!wasNotified) {
      logDecisionChange(
          decision.getId(),
          decision.getDecidedBy(),
          DecisionHistory.ChangeType.NOTIFICATION_STATUS_CHANGED,
          "false",
          "true",
          "notified",
          "Decision notification sent to authors");
    }

    // Log locking
    if (!wasLocked) {
      logDecisionChange(
          decision.getId(),
          decision.getDecidedBy(),
          DecisionHistory.ChangeType.LOCKED,
          "false",
          "true",
          "locked",
          "Decision locked after notification");
    }
  }

  @Transactional
  public void sendBulkNotifications(BulkNotificationRequestDTO dto) {
    NotificationLog.NotificationType notificationType;
    try {
      notificationType = NotificationLog.NotificationType.valueOf(dto.getNotificationType());
    } catch (IllegalArgumentException e) {
      throw new BusinessException("Invalid notification type: " + dto.getNotificationType());
    }

    List<Long> failedSubmissions = new ArrayList<>();

    for (Long submissionId : dto.getSubmissionIds()) {
      Submission submission = submissionRepository.findById(submissionId).orElse(null);
      if (submission == null) {
        continue;
      }

      // Get all authors
      List<SubmissionAuthor> authors = submissionAuthorRepository.findBySubmissionId(submissionId);
      Set<Long> authorIds =
          authors.stream().map(SubmissionAuthor::getUserId).collect(Collectors.toSet());

      for (Long authorId : authorIds) {
        User author = userRepository.findById(authorId).orElse(null);
        if (author != null && author.getEmail() != null) {
          try {
            String subject =
                dto.getCustomSubject() != null
                    ? dto.getCustomSubject()
                    : generateBulkEmailSubject(notificationType, submission.getTitle());
            String content =
                dto.getCustomMessage() != null
                    ? dto.getCustomMessage()
                    : generateBulkEmailContent(notificationType, submission.getTitle());

            // Use EmailService for simple emails
            emailService.sendSimpleEmail(author.getEmail(), subject, content);

            // Log notification
            NotificationLog log =
                NotificationLog.builder()
                    .submissionId(submissionId)
                    .userId(authorId)
                    .type(notificationType)
                    .subject(subject)
                    .content(content)
                    .status(NotificationLog.NotificationStatus.SENT)
                    .build();
            notificationLogRepository.save(log);
          } catch (Exception e) {
            failedSubmissions.add(submissionId);

            // Log failed notification
            NotificationLog log =
                NotificationLog.builder()
                    .submissionId(submissionId)
                    .userId(authorId)
                    .type(notificationType)
                    .subject(dto.getCustomSubject())
                    .content(dto.getCustomMessage())
                    .status(NotificationLog.NotificationStatus.FAILED)
                    .build();
            notificationLogRepository.save(log);
          }
        }
      }
    }

    if (!failedSubmissions.isEmpty()) {
      throw new BusinessException(
          "Failed to send notifications for some submissions: " + failedSubmissions);
    }
  }

  private String generateAnonymizedFeedback(List<Review> reviews) {
    if (reviews.isEmpty()) {
      return "No reviews available.";
    }

    StringBuilder feedback = new StringBuilder();
    feedback.append("Reviewer Feedback (Anonymized):\n\n");

    int reviewNumber = 1;
    for (Review review : reviews) {
      feedback.append("--- Review ").append(reviewNumber).append(" ---\n");

      if (review.getSummary() != null && !review.getSummary().isEmpty()) {
        feedback.append("Summary: ").append(review.getSummary()).append("\n");
      }

      if (review.getStrengths() != null && !review.getStrengths().isEmpty()) {
        feedback.append("Strengths: ").append(review.getStrengths()).append("\n");
      }

      if (review.getWeaknesses() != null && !review.getWeaknesses().isEmpty()) {
        feedback.append("Weaknesses: ").append(review.getWeaknesses()).append("\n");
      }

      if (review.getComments() != null && !review.getComments().isEmpty()) {
        feedback.append("Comments: ").append(review.getComments()).append("\n");
      }

      feedback.append("Score: ").append(review.getScore().name()).append("\n\n");
      reviewNumber++;
    }

    return feedback.toString();
  }

  /** Gửi decision email sử dụng EmailService với template */
  private void sendDecisionEmailWithTemplate(
      User author,
      Submission submission,
      Decision decision,
      String conferenceName,
      String anonymizedFeedback) {
    String subject = generateEmailSubject(decision, submission.getTitle());
    String templateName =
        decision.getType() == Decision.DecisionType.ACCEPT
                || decision.getType() == Decision.DecisionType.CONDITIONAL_ACCEPT
            ? "decision-accept"
            : "decision-reject";

    // Prepare template model
    Map<String, Object> model = new HashMap<>();
    model.put("authorName", author.getFullName());
    model.put("submissionTitle", submission.getTitle());
    model.put("conferenceName", conferenceName);
    model.put("decisionType", decision.getType().name());
    model.put("comments", decision.getComments());
    model.put("anonymizedFeedback", anonymizedFeedback.replace("\n", "<br>"));
    model.put("submissionUrl", frontendUrl + "/submissions/" + submission.getId());

    // Send email using template
    emailService.sendEmail(author.getEmail(), subject, templateName, model);
  }

  private String generateEmailSubject(Decision decision, String submissionTitle) {
    String decisionText =
        decision.getType() == Decision.DecisionType.ACCEPT
            ? "Accepted"
            : decision.getType() == Decision.DecisionType.REJECT
                ? "Rejected"
                : "Conditionally Accepted";
    return String.format("Decision: %s - %s", decisionText, submissionTitle);
  }

  private String generateEmailContent(
      Decision decision, String submissionTitle, String anonymizedFeedback) {
    StringBuilder content = new StringBuilder();
    content.append("Xin chào,\n\n");
    content.append("Chúng tôi xin thông báo quyết định cho submission của bạn:\n\n");
    content.append("Submission: ").append(submissionTitle).append("\n");
    content.append("Decision: ").append(decision.getType().name()).append("\n");

    if (decision.getComments() != null && !decision.getComments().isEmpty()) {
      content.append("Comments: ").append(decision.getComments()).append("\n");
    }

    content.append("\n").append(anonymizedFeedback);
    content.append("\n\n");
    content
        .append("Bạn có thể xem chi tiết tại: ")
        .append(frontendUrl)
        .append("/submissions/")
        .append(decision.getSubmissionId());
    content.append("\n\nTrân trọng,\nUTH-ConfMS Team");

    return content.toString();
  }

  private String generateBulkEmailSubject(
      NotificationLog.NotificationType type, String submissionTitle) {
    return String.format("Notification: %s - %s", type.name(), submissionTitle);
  }

  private String generateBulkEmailContent(
      NotificationLog.NotificationType type, String submissionTitle) {
    return String.format(
        "Xin chào,\n\nThông báo về submission: %s\n\nTrân trọng,\nUTH-ConfMS Team",
        submissionTitle);
  }

  private NotificationLog.NotificationType mapDecisionTypeToNotificationType(
      Decision.DecisionType decisionType) {
    switch (decisionType) {
      case ACCEPT:
        return NotificationLog.NotificationType.DECISION_ACCEPT;
      case REJECT:
        return NotificationLog.NotificationType.DECISION_REJECT;
      case CONDITIONAL_ACCEPT:
        return NotificationLog.NotificationType.DECISION_CONDITIONAL_ACCEPT;
      default:
        return NotificationLog.NotificationType.DECISION_ACCEPT;
    }
  }

  /**
   * Log decision change vào history table
   *
   * @param decisionId Decision ID
   * @param changedBy User ID who made the change
   * @param changeType Type of change
   * @param oldValue Old value (if applicable)
   * @param newValue New value (if applicable)
   * @param fieldName Field name that changed
   * @param description Description of the change
   */
  private void logDecisionChange(
      Long decisionId,
      Long changedBy,
      DecisionHistory.ChangeType changeType,
      String oldValue,
      String newValue,
      String fieldName,
      String description) {
    DecisionHistory history =
        DecisionHistory.builder()
            .decisionId(decisionId)
            .changedBy(changedBy)
            .changeType(changeType)
            .oldValue(oldValue)
            .newValue(newValue)
            .fieldName(fieldName)
            .description(description)
            .build();
    decisionHistoryRepository.save(history);
  }
}
