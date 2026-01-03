package com.uth.confms.submission.controller;

import com.uth.confms.submission.dto.*;
import com.uth.confms.submission.entity.SubmissionFile;
import com.uth.confms.submission.service.FileStorageService;
import com.uth.confms.submission.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controller submission
 */
@Tag(name = "Submission", description = "Submission management APIs")
@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {
    
    private final SubmissionService submissionService;
    private final FileStorageService fileStorageService;
    
    @Operation(summary = "Create a new submission (draft)")
    @PostMapping
    public ResponseEntity<SubmissionResponse> createSubmission(
            @Valid @RequestBody SubmissionRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        SubmissionResponse response = submissionService.createSubmission(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "Get submission by ID")
    @GetMapping("/{id}")
    public ResponseEntity<SubmissionResponse> getSubmission(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        SubmissionResponse response = submissionService.getSubmissionById(id, userId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get my submissions")
    @GetMapping("/my")
    public ResponseEntity<Page<SubmissionResponse>> getMySubmissions(
            @RequestParam(required = false) Long conferenceId,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader("X-User-Id") Long userId) {
        Page<SubmissionResponse> responses = submissionService.getMySubmissions(userId, conferenceId, pageable);
        return ResponseEntity.ok(responses);
    }
    
    @Operation(summary = "Update submission (only DRAFT status)")
    @PutMapping("/{id}")
    public ResponseEntity<SubmissionResponse> updateSubmission(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubmissionRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        SubmissionResponse response = submissionService.updateSubmission(id, request, userId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Submit submission (change status from DRAFT to SUBMITTED)")
    @PostMapping("/{id}/submit")
    public ResponseEntity<SubmissionResponse> submitSubmission(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        SubmissionResponse response = submissionService.submitSubmission(id, userId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Withdraw submission")
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<SubmissionResponse> withdrawSubmission(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestHeader("X-User-Id") Long userId) {
        SubmissionResponse response = submissionService.withdrawSubmission(id, reason, userId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Upload file for submission")
    @PostMapping("/{id}/files")
    public ResponseEntity<FileResponse> uploadFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") SubmissionFile.FileCategory category,
            @RequestHeader("X-User-Id") Long userId) throws IOException {
        FileResponse response = submissionService.uploadFile(id, file, category, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "Delete file from submission")
    @DeleteMapping("/{id}/files/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long id,
            @PathVariable Long fileId,
            @RequestHeader("X-User-Id") Long userId) throws IOException {
        submissionService.deleteFile(id, fileId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Download file")
    @GetMapping("/{id}/files/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            @PathVariable Long fileId,
            @RequestHeader("X-User-Id") Long userId) throws IOException {
        // Validate submission belongs to user
        submissionService.getSubmissionById(id, userId);
        
        // Get file from service
        Resource resource = submissionService.downloadFile(id, fileId, userId);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + 
                        resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}

