package com.uth.confms.submission.service;

import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.conference.entity.Deadline;
import com.uth.confms.conference.entity.Deadline.DeadlineType;
import com.uth.confms.conference.repository.DeadlineRepository;
import com.uth.confms.storage.service.StorageService;
import com.uth.confms.submission.dto.SubmissionAuthorDTO;
import com.uth.confms.submission.dto.SubmissionCreateDTO;
import com.uth.confms.submission.dto.SubmissionFileDTO;
import com.uth.confms.submission.dto.SubmissionResponseDTO;
import com.uth.confms.submission.dto.SubmissionUpdateDTO;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.entity.SubmissionAuthor;
import com.uth.confms.submission.entity.SubmissionFile;
import com.uth.confms.submission.repository.SubmissionAuthorRepository;
import com.uth.confms.submission.repository.SubmissionFileRepository;
import com.uth.confms.submission.repository.SubmissionRepository;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service quản lý submissions (bài nộp)
 *
 * <p>Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 *   <li>Tạo, cập nhật, xóa submission
 *   <li>Upload PDF file
 *   <li>Submit và withdraw submission
 *   <li>Quản lý authors và metadata
 *   <li>Kiểm tra deadline
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class SubmissionService {
  private final SubmissionRepository submissionRepository;
  private final SubmissionAuthorRepository submissionAuthorRepository;
  private final SubmissionFileRepository submissionFileRepository;
  private final DeadlineRepository deadlineRepository;
  private final StorageService storageService;

  public SubmissionService(
      SubmissionRepository submissionRepository,
      SubmissionAuthorRepository submissionAuthorRepository,
      SubmissionFileRepository submissionFileRepository,
      DeadlineRepository deadlineRepository,
      StorageService storageService) {
    this.submissionRepository = submissionRepository;
    this.submissionAuthorRepository = submissionAuthorRepository;
    this.submissionFileRepository = submissionFileRepository;
    this.deadlineRepository = deadlineRepository;
    this.storageService = storageService;
  }

  /**
   * Tạo submission mới
   *
   * @param dto Thông tin submission (title, abstract, keywords, authors)
   * @param authorId ID của author tạo submission
   * @return SubmissionResponseDTO chứa thông tin submission đã tạo
   * @throws BusinessException Nếu deadline đã qua
   */
  @Transactional
  public SubmissionResponseDTO createSubmission(SubmissionCreateDTO dto, Long authorId) {
    // Check if submission deadline has passed
    checkSubmissionDeadline(dto.getConferenceId());

    Submission submission =
        Submission.builder()
            .conferenceId(dto.getConferenceId())
            .authorId(authorId)
            .title(dto.getTitle())
            .abstractText(dto.getAbstractText())
            .trackId(dto.getTrackId())
            .keywords(dto.getKeywords())
            .status(Submission.SubmissionStatus.DRAFT)
            .withdrawn(false)
            .build();

    Submission savedSubmission = submissionRepository.save(submission);

    // Add authors if provided
    if (dto.getAuthors() != null && !dto.getAuthors().isEmpty()) {
      final Submission finalSubmission = savedSubmission;
      List<SubmissionAuthor> authors =
          dto.getAuthors().stream()
              .map(
                  authorDTO ->
                      SubmissionAuthor.builder()
                          .submission(finalSubmission)
                          .userId(authorDTO.getUserId())
                          .firstName(authorDTO.getFirstName())
                          .lastName(authorDTO.getLastName())
                          .email(authorDTO.getEmail())
                          .affiliation(authorDTO.getAffiliation())
                          .isCorresponding(
                              authorDTO.getIsCorresponding() != null
                                  ? authorDTO.getIsCorresponding()
                                  : false)
                          .orderIndex(
                              authorDTO.getOrderIndex() != null ? authorDTO.getOrderIndex() : 0)
                          .build())
              .collect(Collectors.toList());
      submissionAuthorRepository.saveAll(authors);
    }

    return mapToDTO(savedSubmission);
  }

  public SubmissionResponseDTO getSubmission(Long id, Long authorId) {
    Submission submission =
        submissionRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Submission with id " + id + " not found"));

    // Check authorization
    if (!submission.getAuthorId().equals(authorId)) {
      throw new UnauthorizedException("You can only view your own submissions");
    }

    return mapToDTO(submission);
  }

  public List<SubmissionResponseDTO> getMySubmissions(Long authorId) {
    return submissionRepository.findByAuthorId(authorId).stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  @Transactional
  public SubmissionResponseDTO updateSubmission(Long id, SubmissionUpdateDTO dto, Long authorId) {
    Submission submission =
        submissionRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Submission with id " + id + " not found"));

    // Check authorization
    if (!submission.getAuthorId().equals(authorId)) {
      throw new UnauthorizedException("You can only update your own submissions");
    }

    // Check if submission deadline has passed
    checkSubmissionDeadline(submission.getConferenceId());

    // Check if submission can be edited
    if (submission.getStatus() != Submission.SubmissionStatus.DRAFT
        && submission.getStatus() != Submission.SubmissionStatus.SUBMITTED) {
      throw new BusinessException(
          "Cannot edit submission in current status: " + submission.getStatus());
    }

    if (dto.getTitle() != null) {
      submission.setTitle(dto.getTitle());
    }
    if (dto.getAbstractText() != null) {
      submission.setAbstractText(dto.getAbstractText());
    }
    if (dto.getTrackId() != null) {
      submission.setTrackId(dto.getTrackId());
    }
    if (dto.getKeywords() != null) {
      submission.setKeywords(dto.getKeywords());
    }

    // Update authors if provided
    if (dto.getAuthors() != null) {
      submissionAuthorRepository.deleteBySubmission(submission);
      final Submission finalSubmission = submission;
      List<SubmissionAuthor> authors =
          dto.getAuthors().stream()
              .map(
                  authorDTO ->
                      SubmissionAuthor.builder()
                          .submission(finalSubmission)
                          .userId(authorDTO.getUserId())
                          .firstName(authorDTO.getFirstName())
                          .lastName(authorDTO.getLastName())
                          .email(authorDTO.getEmail())
                          .affiliation(authorDTO.getAffiliation())
                          .isCorresponding(
                              authorDTO.getIsCorresponding() != null
                                  ? authorDTO.getIsCorresponding()
                                  : false)
                          .orderIndex(
                              authorDTO.getOrderIndex() != null ? authorDTO.getOrderIndex() : 0)
                          .build())
              .collect(Collectors.toList());
      submissionAuthorRepository.saveAll(authors);
    }

    Submission updatedSubmission = submissionRepository.save(submission);
    return mapToDTO(updatedSubmission);
  }

  @Transactional
  public SubmissionResponseDTO submitSubmission(Long id, Long authorId) {
    Submission submission =
        submissionRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Submission with id " + id + " not found"));

    // Check authorization
    if (!submission.getAuthorId().equals(authorId)) {
      throw new UnauthorizedException("You can only submit your own submissions");
    }

    // Check if submission deadline has passed
    checkSubmissionDeadline(submission.getConferenceId());

    // Check if PDF is uploaded
    if (submission.getPdfFilePath() == null || submission.getPdfFilePath().isEmpty()) {
      throw new BusinessException("PDF file is required before submission");
    }

    submission.setStatus(Submission.SubmissionStatus.SUBMITTED);
    Submission updatedSubmission = submissionRepository.save(submission);

    return mapToDTO(updatedSubmission);
  }

  @Transactional
  public SubmissionResponseDTO withdrawSubmission(Long id, Long authorId) {
    Submission submission =
        submissionRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Submission with id " + id + " not found"));

    // Check authorization
    if (!submission.getAuthorId().equals(authorId)) {
      throw new UnauthorizedException("You can only withdraw your own submissions");
    }

    // Check if submission deadline has passed
    checkSubmissionDeadline(submission.getConferenceId());

    // Check if submission can be withdrawn
    if (submission.getStatus() == Submission.SubmissionStatus.ACCEPTED
        || submission.getStatus() == Submission.SubmissionStatus.CAMERA_READY) {
      throw new BusinessException("Cannot withdraw accepted submissions");
    }

    submission.setWithdrawn(true);
    Submission updatedSubmission = submissionRepository.save(submission);

    return mapToDTO(updatedSubmission);
  }

  @Transactional
  public SubmissionFileDTO uploadPdf(Long submissionId, MultipartFile file, Long authorId)
      throws IOException {
    Submission submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(
                () -> new NotFoundException("Submission with id " + submissionId + " not found"));

    // Check authorization
    if (!submission.getAuthorId().equals(authorId)) {
      throw new UnauthorizedException("You can only upload files for your own submissions");
    }

    // Check if submission deadline has passed
    checkSubmissionDeadline(submission.getConferenceId());

    // Store file using StorageService (validation is done inside)
    String relativePath = storageService.storeSubmissionPdf(submissionId, file);

    // Calculate checksum
    String checksum = calculateChecksumFromStream(storageService.getFileStream(relativePath));

    // Get next version number
    Integer versionNumber = submissionFileRepository.countBySubmission(submission) + 1;

    // Mark previous files as not current
    submissionFileRepository
        .findBySubmissionAndIsCurrentTrue(submission)
        .ifPresent(
            prevFile -> {
              prevFile.setIsCurrent(false);
              submissionFileRepository.save(prevFile);
            });

    // Create submission file record
    SubmissionFile submissionFile =
        SubmissionFile.builder()
            .submission(submission)
            .versionNumber(versionNumber)
            .filePath(relativePath)
            .fileName(file.getOriginalFilename())
            .fileSize(file.getSize())
            .contentType(file.getContentType())
            .checksum(checksum)
            .isCurrent(true)
            .build();

    SubmissionFile savedFile = submissionFileRepository.save(submissionFile);

    // Update submission PDF path
    submission.setPdfFilePath(relativePath);
    submissionRepository.save(submission);

    return mapFileToDTO(savedFile);
  }

  private void checkSubmissionDeadline(Long conferenceId) {
    List<Deadline> deadlines = deadlineRepository.findByConferenceId(conferenceId);
    Deadline submissionDeadline =
        deadlines.stream()
            .filter(d -> d.getType() == DeadlineType.SUBMISSION)
            .findFirst()
            .orElse(null);

    if (submissionDeadline != null
        && submissionDeadline.getDueDate().isBefore(LocalDateTime.now())) {
      if (submissionDeadline.getHardDeadline()) {
        throw new BusinessException("Submission deadline has passed");
      }
    }
  }

  /**
   * Tính checksum từ InputStream
   *
   * @param inputStream InputStream của file
   * @return MD5 checksum dạng hex string
   */
  private String calculateChecksumFromStream(InputStream inputStream) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] buffer = new byte[8192];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        md.update(buffer, 0, bytesRead);
      }
      byte[] digest = md.digest();
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException("Error calculating checksum", e);
    } finally {
      try {
        inputStream.close();
      } catch (IOException e) {
        // Ignore
      }
    }
  }

  private SubmissionResponseDTO mapToDTO(Submission submission) {
    List<SubmissionAuthorDTO> authors =
        submissionAuthorRepository.findBySubmission(submission).stream()
            .map(this::mapAuthorToDTO)
            .collect(Collectors.toList());

    List<SubmissionFileDTO> files =
        submissionFileRepository.findBySubmission(submission).stream()
            .map(this::mapFileToDTO)
            .collect(Collectors.toList());

    return SubmissionResponseDTO.builder()
        .id(submission.getId())
        .conferenceId(submission.getConferenceId())
        .authorId(submission.getAuthorId())
        .title(submission.getTitle())
        .abstractText(submission.getAbstractText())
        .status(submission.getStatus().name())
        .pdfFilePath(submission.getPdfFilePath())
        .trackId(submission.getTrackId())
        .keywords(submission.getKeywords())
        .withdrawn(submission.getWithdrawn())
        .authors(authors)
        .files(files)
        .createdAt(submission.getCreatedAt())
        .updatedAt(submission.getUpdatedAt())
        .build();
  }

  private SubmissionAuthorDTO mapAuthorToDTO(SubmissionAuthor author) {
    return SubmissionAuthorDTO.builder()
        .id(author.getId())
        .userId(author.getUserId())
        .firstName(author.getFirstName())
        .lastName(author.getLastName())
        .email(author.getEmail())
        .affiliation(author.getAffiliation())
        .isCorresponding(author.getIsCorresponding())
        .orderIndex(author.getOrderIndex())
        .build();
  }

  private SubmissionFileDTO mapFileToDTO(SubmissionFile file) {
    return SubmissionFileDTO.builder()
        .id(file.getId())
        .versionNumber(file.getVersionNumber())
        .fileName(file.getFileName())
        .filePath(file.getFilePath())
        .fileSize(file.getFileSize())
        .contentType(file.getContentType())
        .isCurrent(file.getIsCurrent())
        .uploadedAt(file.getUploadedAt())
        .uploadNote(file.getUploadNote())
        .build();
  }
}
