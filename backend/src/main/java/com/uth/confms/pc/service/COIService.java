package com.uth.confms.pc.service;

import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.pc.dto.COIDeclareDTO;
import com.uth.confms.pc.entity.ConflictOfInterest;
import com.uth.confms.pc.repository.ConflictOfInterestRepository;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.entity.SubmissionAuthor;
import com.uth.confms.submission.repository.SubmissionAuthorRepository;
import com.uth.confms.submission.repository.SubmissionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý Conflict of Interest (COI) declarations
 *
 * <p>Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 *   <li>Khai báo COI giữa reviewer và submission
 *   <li>Tự động phát hiện COI (nếu reviewer là author)
 *   <li>Kiểm tra COI trước khi assign review
 *   <li>Quản lý COI records
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class COIService {
  private final ConflictOfInterestRepository coiRepository;
  private final SubmissionRepository submissionRepository;
  private final SubmissionAuthorRepository submissionAuthorRepository;

  public COIService(
      ConflictOfInterestRepository coiRepository,
      SubmissionRepository submissionRepository,
      SubmissionAuthorRepository submissionAuthorRepository) {
    this.coiRepository = coiRepository;
    this.submissionRepository = submissionRepository;
    this.submissionAuthorRepository = submissionAuthorRepository;
  }

  @Transactional
  public ConflictOfInterest declareCOI(COIDeclareDTO dto, Long reviewerId) {
    // Validate submission exists
    submissionRepository
        .findById(dto.getSubmissionId())
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Submission with id " + dto.getSubmissionId() + " not found"));

    // Check if COI already exists
    coiRepository
        .findByReviewerIdAndSubmissionId(reviewerId, dto.getSubmissionId())
        .ifPresent(
            existing -> {
              if (existing.getActive()) {
                throw new BusinessException("COI already declared for this submission");
              }
            });

    ConflictOfInterest coi =
        ConflictOfInterest.builder()
            .reviewerId(reviewerId)
            .submissionId(dto.getSubmissionId())
            .type(ConflictOfInterest.COIType.valueOf(dto.getType()))
            .reason(dto.getReason())
            .active(true)
            .build();

    return coiRepository.save(coi);
  }

  @Transactional
  public void removeCOI(Long coiId, Long reviewerId) {
    ConflictOfInterest coi =
        coiRepository
            .findById(coiId)
            .orElseThrow(() -> new NotFoundException("COI record not found"));

    // Check authorization
    if (!coi.getReviewerId().equals(reviewerId)) {
      throw new BusinessException("You can only remove your own COI declarations");
    }

    coi.setActive(false);
    coiRepository.save(coi);
  }

  public List<ConflictOfInterest> getCOIsByReviewer(Long reviewerId) {
    return coiRepository.findByReviewerIdAndActiveTrue(reviewerId);
  }

  public List<ConflictOfInterest> getCOIsBySubmission(Long submissionId) {
    return coiRepository.findBySubmissionIdAndActiveTrue(submissionId);
  }

  public boolean hasCOI(Long reviewerId, Long submissionId) {
    return coiRepository
        .findByReviewerIdAndSubmissionId(reviewerId, submissionId)
        .map(ConflictOfInterest::getActive)
        .orElse(false);
  }

  @Transactional
  public void detectAndSuggestCOI(Long reviewerId, Long submissionId) {
    Submission submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(() -> new NotFoundException("Submission not found"));

    // Check if reviewer is an author of the submission
    List<SubmissionAuthor> authors = submissionAuthorRepository.findBySubmission(submission);
    boolean isAuthor = authors.stream().anyMatch(author -> author.getUserId().equals(reviewerId));

    if (isAuthor) {
      // Auto-declare COI
      if (!hasCOI(reviewerId, submissionId)) {
        ConflictOfInterest coi =
            ConflictOfInterest.builder()
                .reviewerId(reviewerId)
                .submissionId(submissionId)
                .type(ConflictOfInterest.COIType.CO_AUTHOR)
                .reason("Reviewer is an author of this submission")
                .active(true)
                .build();
        coiRepository.save(coi);
      }
    }
  }
}
