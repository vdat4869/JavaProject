package com.uth.confms.submission.service;

import com.uth.confms.submission.dto.*;
import com.uth.confms.submission.entity.Author;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.entity.SubmissionFile;
import com.uth.confms.submission.exception.SubmissionException;
import com.uth.confms.submission.mapper.SubmissionMapper;
import com.uth.confms.submission.repository.AuthorRepository;
import com.uth.confms.submission.repository.SubmissionFileRepository;
import com.uth.confms.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {
    
    private final SubmissionRepository submissionRepository;
    private final AuthorRepository authorRepository;
    private final SubmissionFileRepository fileRepository;
    private final FileStorageService fileStorageService;
    private final SubmissionMapper submissionMapper;
    
    /**
     * Tạo submission mới (draft)
     */
    @Transactional
    public SubmissionResponse createSubmission(SubmissionRequest request, Long submitterId) {
        // Validate
        validateSubmissionRequest(request);
        
        // Tạo submission
        Submission submission = Submission.builder()
                .conferenceId(request.getConferenceId())
                .trackId(request.getTrackId())
                .submitterId(submitterId)
                .title(request.getTitle())
                .abstractText(request.getAbstractText())
                .keywords(request.getKeywords())
                .notes(request.getNotes())
                .status(Submission.SubmissionStatus.DRAFT)
                .type(Submission.SubmissionType.RESEARCH_PAPER)
                .isBlind(true)
                .isWithdrawn(false)
                .build();
        
        // Generate submission number
        submission.setSubmissionNumber(generateSubmissionNumber(request.getConferenceId()));
        
        submission = submissionRepository.save(submission);
        
        // Tạo authors
        List<Author> authors = request.getAuthors().stream()
                .map(authorRequest -> {
                    Author author = submissionMapper.toAuthor(authorRequest);
                    author.setSubmission(submission);
                    return author;
                })
                .collect(Collectors.toList());
        
        // Đảm bảo có ít nhất 1 corresponding author
        if (authors.stream().noneMatch(a -> Boolean.TRUE.equals(a.getIsCorresponding()))) {
            authors.get(0).setIsCorresponding(true);
        }
        
        authorRepository.saveAll(authors);
        submission.setAuthors(authors);
        
        log.info("Created submission {} by user {}", submission.getId(), submitterId);
        return submissionMapper.toResponse(submission);
    }
    
    /**
     * Cập nhật submission (chỉ khi ở trạng thái DRAFT)
     */
    @Transactional
    public SubmissionResponse updateSubmission(Long submissionId, UpdateSubmissionRequest request, Long userId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionException("Submission not found"));
        
        // Kiểm tra quyền
        if (!submission.getSubmitterId().equals(userId)) {
            throw new SubmissionException("You don't have permission to update this submission");
        }
        
        // Chỉ cho phép update khi ở trạng thái DRAFT
        if (submission.getStatus() != Submission.SubmissionStatus.DRAFT) {
            throw new SubmissionException("Cannot update submission. Only DRAFT submissions can be updated");
        }
        
        // Update fields
        submissionMapper.updateSubmissionFromRequest(request, submission);
        
        if (request.getTitle() != null) {
            submission.setTitle(request.getTitle());
        }
        if (request.getAbstractText() != null) {
            submission.setAbstractText(request.getAbstractText());
        }
        if (request.getKeywords() != null) {
            submission.setKeywords(request.getKeywords());
        }
        if (request.getNotes() != null) {
            submission.setNotes(request.getNotes());
        }
        
        // Update authors nếu có
        if (request.getAuthors() != null && !request.getAuthors().isEmpty()) {
            // Xóa authors cũ
            authorRepository.deleteAll(submission.getAuthors());
            
            // Tạo authors mới
            List<Author> authors = request.getAuthors().stream()
                    .map(authorRequest -> {
                        Author author = submissionMapper.toAuthor(authorRequest);
                        author.setSubmission(submission);
                        return author;
                    })
                    .collect(Collectors.toList());
            
            if (authors.stream().noneMatch(a -> Boolean.TRUE.equals(a.getIsCorresponding()))) {
                authors.get(0).setIsCorresponding(true);
            }
            
            authorRepository.saveAll(authors);
            submission.setAuthors(authors);
        }
        
        submission = submissionRepository.save(submission);
        log.info("Updated submission {} by user {}", submissionId, userId);
        return submissionMapper.toResponse(submission);
    }
    
    /**
     * Submit submission (chuyển từ DRAFT sang SUBMITTED)
     */
    @Transactional
    public SubmissionResponse submitSubmission(Long submissionId, Long userId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionException("Submission not found"));
        
        // Kiểm tra quyền
        if (!submission.getSubmitterId().equals(userId)) {
            throw new SubmissionException("You don't have permission to submit this submission");
        }
        
        // Chỉ cho phép submit khi ở trạng thái DRAFT
        if (submission.getStatus() != Submission.SubmissionStatus.DRAFT) {
            throw new SubmissionException("Submission is already submitted or in another state");
        }
        
        // Validate: phải có ít nhất 1 file manuscript
        List<SubmissionFile> manuscriptFiles = fileRepository.findBySubmissionIdAndCategory(
                submissionId, SubmissionFile.FileCategory.MANUSCRIPT);
        if (manuscriptFiles.isEmpty()) {
            throw new SubmissionException("Cannot submit. Please upload at least one manuscript file");
        }
        
        // Chuyển trạng thái
        submission.setStatus(Submission.SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());
        
        submission = submissionRepository.save(submission);
        log.info("Submitted submission {} by user {}", submissionId, userId);
        return submissionMapper.toResponse(submission);
    }
    
    /**
     * Rút bài (withdraw)
     */
    @Transactional
    public SubmissionResponse withdrawSubmission(Long submissionId, String reason, Long userId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionException("Submission not found"));
        
        // Kiểm tra quyền
        if (!submission.getSubmitterId().equals(userId)) {
            throw new SubmissionException("You don't have permission to withdraw this submission");
        }
        
        // Chỉ cho phép withdraw khi chưa được accept/reject
        if (submission.getStatus() == Submission.SubmissionStatus.ACCEPTED ||
            submission.getStatus() == Submission.SubmissionStatus.REJECTED) {
            throw new SubmissionException("Cannot withdraw. Submission is already " + submission.getStatus());
        }
        
        submission.setStatus(Submission.SubmissionStatus.WITHDRAWN);
        submission.setIsWithdrawn(true);
        submission.setWithdrawnAt(LocalDateTime.now());
        submission.setWithdrawReason(reason);
        
        submission = submissionRepository.save(submission);
        log.info("Withdrawn submission {} by user {}", submissionId, userId);
        return submissionMapper.toResponse(submission);
    }
    
    /**
     * Upload file cho submission
     */
    @Transactional
    public FileResponse uploadFile(Long submissionId, MultipartFile file, 
                                   SubmissionFile.FileCategory category, Long userId) throws IOException {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionException("Submission not found"));
        
        // Kiểm tra quyền
        if (!submission.getSubmitterId().equals(userId)) {
            throw new SubmissionException("You don't have permission to upload file for this submission");
        }
        
        // Chỉ cho phép upload khi ở trạng thái DRAFT hoặc REVISION
        if (submission.getStatus() != Submission.SubmissionStatus.DRAFT &&
            submission.getStatus() != Submission.SubmissionStatus.REVISION) {
            throw new SubmissionException("Cannot upload file. Submission is not in DRAFT or REVISION state");
        }
        
        // Validate file
        validateFile(file);
        
        // Lưu file
        String subdirectory = String.format("submissions/%d/%s", submissionId, category.name().toLowerCase());
        String storedPath = fileStorageService.storeFile(file, subdirectory);
        
        // Tính checksum
        String checksum = calculateChecksum(file.getBytes());
        
        // Đánh dấu các file cũ không phải latest
        List<SubmissionFile> oldFiles = fileRepository.findBySubmissionIdAndCategory(submissionId, category);
        oldFiles.forEach(f -> f.setIsLatest(false));
        fileRepository.saveAll(oldFiles);
        
        // Tạo file record
        int nextVersion = oldFiles.isEmpty() ? 1 : oldFiles.stream()
                .mapToInt(SubmissionFile::getVersion)
                .max()
                .orElse(0) + 1;
        
        SubmissionFile submissionFile = SubmissionFile.builder()
                .submission(submission)
                .fileName(file.getOriginalFilename())
                .storedPath(storedPath)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .category(category)
                .version(nextVersion)
                .isLatest(true)
                .checksum(checksum)
                .uploadedBy(userId)
                .build();
        
        submissionFile = fileRepository.save(submissionFile);
        log.info("Uploaded file {} for submission {} by user {}", file.getOriginalFilename(), submissionId, userId);
        
        FileResponse response = submissionMapper.toFileResponse(submissionFile);
        response.setDownloadUrl("/api/submissions/" + submissionId + "/files/" + submissionFile.getId() + "/download");
        return response;
    }
    
    /**
     * Download file
     */
    public org.springframework.core.io.Resource downloadFile(Long submissionId, Long fileId, Long userId) throws IOException {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionException("Submission not found"));
        
        // Kiểm tra quyền
        if (!submission.getSubmitterId().equals(userId)) {
            throw new SubmissionException("You don't have permission to download file from this submission");
        }
        
        SubmissionFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new SubmissionException("File not found"));
        
        if (!file.getSubmission().getId().equals(submissionId)) {
            throw new SubmissionException("File does not belong to this submission");
        }
        
        InputStream inputStream = fileStorageService.getFileInputStream(file.getStoredPath());
        return new org.springframework.core.io.InputStreamResource(inputStream) {
            @Override
            public String getFilename() {
                return file.getFileName();
            }
        };
    }
    
    /**
     * Xóa file
     */
    @Transactional
    public void deleteFile(Long submissionId, Long fileId, Long userId) throws IOException {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionException("Submission not found"));
        
        // Kiểm tra quyền
        if (!submission.getSubmitterId().equals(userId)) {
            throw new SubmissionException("You don't have permission to delete file from this submission");
        }
        
        SubmissionFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new SubmissionException("File not found"));
        
        if (!file.getSubmission().getId().equals(submissionId)) {
            throw new SubmissionException("File does not belong to this submission");
        }
        
        // Xóa file từ storage
        fileStorageService.deleteFile(file.getStoredPath());
        
        // Xóa record
        fileRepository.delete(file);
        log.info("Deleted file {} from submission {} by user {}", fileId, submissionId, userId);
    }
    
    /**
     * Lấy submission theo ID
     */
    public SubmissionResponse getSubmissionById(Long submissionId, Long userId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionException("Submission not found"));
        
        // Kiểm tra quyền (chỉ submitter mới xem được)
        if (!submission.getSubmitterId().equals(userId)) {
            throw new SubmissionException("You don't have permission to view this submission");
        }
        
        return submissionMapper.toResponse(submission);
    }
    
    /**
     * Lấy danh sách submissions của user
     */
    public Page<SubmissionResponse> getMySubmissions(Long userId, Long conferenceId, Pageable pageable) {
        Page<Submission> submissions;
        if (conferenceId != null) {
            submissions = submissionRepository.findBySubmitterIdAndConferenceId(userId, conferenceId, pageable);
        } else {
            submissions = submissionRepository.findBySubmitterId(userId, pageable);
        }
        return submissions.map(submissionMapper::toResponse);
    }
    
    // ========== Helper Methods ==========
    
    private void validateSubmissionRequest(SubmissionRequest request) {
        if (request.getAuthors() == null || request.getAuthors().isEmpty()) {
            throw new SubmissionException("At least one author is required");
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new SubmissionException("File is empty");
        }
        
        // Kiểm tra file type (chỉ cho phép PDF)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new SubmissionException("Only PDF files are allowed");
        }
        
        // Kiểm tra file size (max 50MB)
        long maxSize = 50 * 1024 * 1024; // 50MB
        if (file.getSize() > maxSize) {
            throw new SubmissionException("File size exceeds 50MB limit");
        }
    }
    
    private String generateSubmissionNumber(Long conferenceId) {
        // Format: CONF-YYYY-XXX
        String year = String.valueOf(Year.now().getValue());
        long count = submissionRepository.count() + 1;
        return String.format("CONF-%s-%03d", year, count);
    }
    
    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Error calculating checksum", e);
            return "";
        }
    }
}

