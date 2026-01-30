package com.uth.confms.submission.controller;

import com.uth.confms.auth.service.UserService;
import com.uth.confms.common.dto.ApiResponse;
import com.uth.confms.submission.dto.SubmissionCreateDTO;
import com.uth.confms.submission.dto.SubmissionFileDTO;
import com.uth.confms.submission.dto.SubmissionResponseDTO;
import com.uth.confms.submission.dto.SubmissionUpdateDTO;
import com.uth.confms.submission.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller quản lý submissions (bài nộp)
 *
 * <p>
 * Các endpoints:
 *
 * <ul>
 * <li>GET /api/submissions/my - Lấy danh sách submissions của author (AUTHOR)
 * <li>GET /api/submissions/{id} - Lấy thông tin submission (AUTHOR)
 * <li>GET /api/submissions/conference/{conferenceId} - Lấy danh sách
 * submissions của conference (CHAIR/ADMIN)
 * <li>POST /api/submissions - Tạo submission mới (AUTHOR)
 * <li>PUT /api/submissions/{id} - Cập nhật submission (AUTHOR)
 * <li>POST /api/submissions/{id}/submit - Submit submission (AUTHOR)
 * <li>POST /api/submissions/{id}/withdraw - Withdraw submission (AUTHOR)
 * <li>POST /api/submissions/{id}/upload-pdf - Upload PDF file (AUTHOR)
 * <li>GET /api/submissions/{id}/file - Download PDF file hiện tại (AUTHOR)
 * <li>GET /api/submissions/{id}/files - Xem lịch sử upload PDF (AUTHOR)
 * <li>GET /api/submissions/{id}/files/{fileId} - Download file version cụ thể
 * (AUTHOR)
 * <li>DELETE /api/submissions/{id} - Xóa submission draft (AUTHOR)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/submissions")
@Tag(name = "Submission", description = "API quản lý submissions (bài nộp) của tác giả")
@SecurityRequirement(name = "Bearer Authentication")
public class SubmissionController {
    private final SubmissionService submissionService;
    private final UserService userService;

    public SubmissionController(SubmissionService submissionService, UserService userService) {
        this.submissionService = submissionService;
        this.userService = userService;
    }

    @Operation(summary = "Lấy danh sách submissions của tác giả", description = "Trả về danh sách tất cả submissions của tác giả hiện tại")
    @GetMapping("/my")
    @PreAuthorize("hasRole('AUTHOR')")
    // Lấy danh sách submissions của tác giả (My Submissions)
    public ResponseEntity<ApiResponse<List<SubmissionResponseDTO>>> getMySubmissions(
            Authentication authentication) {
        Long authorId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(ApiResponse.success(submissionService.getMySubmissions(authorId)));
    }

    @Operation(summary = "Lấy thông tin submission", description = "Trả về thông tin chi tiết của một submission theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('AUTHOR') or hasRole('CHAIR') or hasRole('PC') or hasRole('ADMIN')")
    // Lấy chi tiết submission
    public ResponseEntity<ApiResponse<SubmissionResponseDTO>> getSubmission(
            @Parameter(description = "ID của submission") @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(ApiResponse.success(submissionService.getSubmission(id, userId)));
    }

    @Operation(summary = "Tạo submission mới", description = "Tạo một submission mới với thông tin title, abstract, keywords, authors")
    @PostMapping
    @PreAuthorize("hasRole('AUTHOR')")
    // Tạo submission mới
    public ResponseEntity<ApiResponse<SubmissionResponseDTO>> createSubmission(
            @Valid @RequestBody SubmissionCreateDTO dto, Authentication authentication) {
        Long authorId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(
                ApiResponse.success(submissionService.createSubmission(dto, authorId)));
    }

    @Operation(summary = "Cập nhật submission", description = "Cập nhật thông tin submission (chỉ cho phép khi status là DRAFT hoặc SUBMITTED)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AUTHOR')")
    // Cập nhật submission
    public ResponseEntity<ApiResponse<SubmissionResponseDTO>> updateSubmission(
            @Parameter(description = "ID của submission") @PathVariable Long id,
            @Valid @RequestBody SubmissionUpdateDTO dto,
            Authentication authentication) {
        Long authorId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(
                ApiResponse.success(submissionService.updateSubmission(id, dto, authorId)));
    }

    @Operation(summary = "Submit submission", description = "Nộp submission (chuyển từ DRAFT sang SUBMITTED, yêu cầu phải có PDF file)")
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('AUTHOR')")
    // Submit bài (chuyển trạng thái sang SUBMITTED)
    public ResponseEntity<ApiResponse<SubmissionResponseDTO>> submitSubmission(
            @Parameter(description = "ID của submission") @PathVariable Long id,
            Authentication authentication) {
        Long authorId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(ApiResponse.success(submissionService.submitSubmission(id, authorId)));
    }

    @Operation(summary = "Rút submission", description = "Rút submission đã submit (không cho phép rút nếu đã ACCEPTED hoặc CAMERA_READY)")
    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('AUTHOR')")
    // Rút bài (withdraw)
    public ResponseEntity<ApiResponse<SubmissionResponseDTO>> withdrawSubmission(
            @Parameter(description = "ID của submission") @PathVariable Long id,
            Authentication authentication) {
        Long authorId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(
                ApiResponse.success(submissionService.withdrawSubmission(id, authorId)));
    }

    @Operation(summary = "Upload PDF file", description = "Upload PDF file cho submission (hỗ trợ nhiều version, version mới sẽ là current)")
    @PostMapping("/{id}/upload-pdf")
    @PreAuthorize("hasRole('AUTHOR')")
    // Upload file PDF
    public ResponseEntity<ApiResponse<SubmissionFileDTO>> uploadPdf(
            @Parameter(description = "ID của submission") @PathVariable Long id,
            @Parameter(description = "File PDF cần upload") @RequestParam("file") MultipartFile file,
            Authentication authentication)
            throws IOException {
        Long authorId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(ApiResponse.success(submissionService.uploadPdf(id, file, authorId)));
    }

    @Operation(summary = "Download PDF file hiện tại", description = "Download file PDF hiện tại (current version) của submission")
    @GetMapping("/{id}/file")
    @PreAuthorize("hasRole('AUTHOR') or hasRole('CHAIR') or hasRole('PC') or hasRole('ADMIN')")
    // Tải file PDF hiện tại
    public ResponseEntity<InputStreamResource> downloadPdfFile(
            @Parameter(description = "ID của submission") @PathVariable Long id,
            Authentication authentication)
            throws IOException {
        Long userId = getUserIdFromAuthentication(authentication);
        InputStream fileStream = submissionService.downloadPdfFile(id, userId);
        SubmissionResponseDTO submission = submissionService.getSubmission(id, userId);

        @SuppressWarnings("null")
        InputStreamResource resource = new InputStreamResource(fileStream);
        String fileName = submission.getPdfFilePath() != null
                ? submission.getPdfFilePath().substring(submission.getPdfFilePath().lastIndexOf('/') + 1)
                : "submission-" + id + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.valueOf("application/pdf"))
                .body(resource);
    }

    @Operation(summary = "Xem lịch sử upload PDF", description = "Lấy danh sách tất cả các version của PDF file đã upload cho submission")
    @GetMapping("/{id}/files")
    @PreAuthorize("hasRole('AUTHOR') or hasRole('CHAIR') or hasRole('PC') or hasRole('ADMIN')")
    // Xem lịch sử các phiên bản file
    public ResponseEntity<ApiResponse<List<SubmissionFileDTO>>> getFileVersions(
            @Parameter(description = "ID của submission") @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(
                ApiResponse.success(submissionService.getFileVersions(id, userId)));
    }

    @Operation(summary = "Download file version cụ thể", description = "Download một version cụ thể của PDF file theo fileId")
    @GetMapping("/{id}/files/{fileId}")
    @PreAuthorize("hasRole('AUTHOR') or hasRole('CHAIR') or hasRole('PC') or hasRole('ADMIN')")
    // Tải một phiên bản file cụ thể
    public ResponseEntity<InputStreamResource> downloadFileVersion(
            @Parameter(description = "ID của submission") @PathVariable Long id,
            @Parameter(description = "ID của file version") @PathVariable Long fileId,
            Authentication authentication)
            throws IOException {
        Long authorId = getUserIdFromAuthentication(authentication);
        InputStream fileStream = submissionService.downloadFileVersion(id, fileId, authorId);

        // Get file info for filename
        List<SubmissionFileDTO> files = submissionService.getFileVersions(id, authorId);
        SubmissionFileDTO file = files.stream()
                .filter(f -> f.getId().equals(fileId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("File not found"));

        @SuppressWarnings("null")
        InputStreamResource resource = new InputStreamResource(fileStream);
        String fileName = file.getFileName() != null ? file.getFileName() : "file-" + fileId + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.valueOf("application/pdf"))
                .body(resource);
    }

    @Operation(summary = "Xóa submission draft", description = "Xóa submission (chỉ cho phép xóa submission ở trạng thái DRAFT)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AUTHOR')")
    // Xóa submission (chỉ xóa được bản nháp DRAFT)
    public ResponseEntity<ApiResponse<Void>> deleteSubmission(
            @Parameter(description = "ID của submission") @PathVariable Long id,
            Authentication authentication) {
        Long authorId = getUserIdFromAuthentication(authentication);
        submissionService.deleteSubmission(id, authorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "Lấy danh sách submissions của conference", description = "Trả về danh sách tất cả submissions của một conference (CHAIR/ADMIN only)")
    @GetMapping("/conference/{conferenceId}")
    @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
    // Lấy danh sách submission của hội nghị (dành cho Chair/Admin)
    public ResponseEntity<ApiResponse<List<SubmissionResponseDTO>>> getSubmissionsByConference(
            @Parameter(description = "ID của conference") @PathVariable Long conferenceId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(
                ApiResponse.success(submissionService.getSubmissionsByConference(conferenceId, userId)));
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        return userService.getUserIdByEmail(email);
    }
}
