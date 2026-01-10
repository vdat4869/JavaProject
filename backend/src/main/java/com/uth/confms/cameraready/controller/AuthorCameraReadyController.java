package com.uth.confms.cameraready.controller;

import com.uth.confms.cameraready.dto.*;
import com.uth.confms.cameraready.service.CameraReadyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Controller cho các API camera-ready dành cho tác giả.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/conferences/{conferenceId}/camera-ready/papers/{paperId}")
@RequiredArgsConstructor
@Tag(name = "Camera-ready - Tác giả", description = "API quản lý camera-ready cho tác giả")
public class AuthorCameraReadyController {

    private final CameraReadyService cameraReadyService;

    @GetMapping
    @Operation(summary = "Lấy trạng thái bài nộp")
    public ResponseEntity<SubmissionDTO> getSubmission(
            @PathVariable UUID conferenceId,
            @PathVariable UUID paperId) {
        
        log.debug("GET /conferences/{}/camera-ready/papers/{}", conferenceId, paperId);
        SubmissionDTO submission = cameraReadyService.getSubmissionByPaperId(conferenceId, paperId);
        return ResponseEntity.ok(submission);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tải lên PDF")
    public ResponseEntity<VersionDTO> uploadVersion(
            @PathVariable UUID conferenceId,
            @PathVariable UUID paperId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") UUID userId) {
        
        log.info("POST upload for paper {} by user {}", paperId, userId);
        VersionDTO version = cameraReadyService.uploadVersion(conferenceId, paperId, file, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(version);
    }

    @GetMapping("/versions")
    @Operation(summary = "Danh sách phiên bản")
    public ResponseEntity<List<VersionDTO>> listVersions(
            @PathVariable UUID conferenceId,
            @PathVariable UUID paperId) {
        
        List<VersionDTO> versions = cameraReadyService.listVersions(conferenceId, paperId);
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/versions/{versionId}/download")
    @Operation(summary = "Tải xuống phiên bản")
    public ResponseEntity<Resource> downloadVersion(
            @PathVariable UUID conferenceId,
            @PathVariable UUID paperId,
            @PathVariable UUID versionId) {
        
        Resource resource = cameraReadyService.downloadVersion(conferenceId, paperId, versionId);
        String filename = cameraReadyService.getVersionFilename(versionId);
        
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("application/pdf"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @PostMapping("/confirm-copyright")
    @Operation(summary = "Xác nhận bản quyền")
    public ResponseEntity<SubmissionDTO> confirmCopyright(
            @PathVariable UUID conferenceId,
            @PathVariable UUID paperId,
            @Valid @RequestBody CopyrightConfirmRequestDTO request,
            @RequestHeader("X-User-Id") UUID userId) {
        
        log.info("POST confirm-copyright for paper {} by user {}", paperId, userId);
        SubmissionDTO submission = cameraReadyService.confirmCopyright(conferenceId, paperId, request, userId);
        return ResponseEntity.ok(submission);
    }
}
