package com.uth.confms.cameraready.controller;

import com.uth.confms.cameraready.dto.*;
import com.uth.confms.cameraready.entity.CameraReadyStatus;
import com.uth.confms.cameraready.service.CameraReadyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Controller cho các API camera-ready dành cho Chair.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/conferences/{conferenceId}/camera-ready")
@RequiredArgsConstructor
@Tag(name = "Camera-ready - Chair", description = "API quản lý camera-ready cho Chair")
public class ChairCameraReadyController {

        private final CameraReadyService cameraReadyService;

        @GetMapping("/submissions")
        @Operation(summary = "Lấy danh sách bài nộp camera-ready (có phân trang và lọc)")
        public ResponseEntity<Page<SubmissionListDTO>> listSubmissions(
                        @PathVariable Long conferenceId,
                        @RequestParam(required = false) Long trackId,
                        @RequestParam(required = false) CameraReadyStatus status,
                        @RequestParam(required = false) Boolean copyrightConfirmed,
                        @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {

                Page<SubmissionListDTO> page = cameraReadyService.listSubmissions(
                                conferenceId, trackId, status, copyrightConfirmed, pageable);
                return ResponseEntity.ok(page);
        }

        @PostMapping("/submissions/{submissionId}/review")
        @Operation(summary = "Duyệt hoặc yêu cầu chỉnh sửa bài nộp")
        public ResponseEntity<ReviewResponseDTO> reviewSubmission(
                        @PathVariable Long conferenceId,
                        @PathVariable UUID submissionId,
                        @Valid @RequestBody ReviewRequestDTO request,
                        @RequestHeader("X-User-Id") Long userId) {

                log.info("POST review submission {} by user {}", submissionId, userId);
                ReviewResponseDTO response = cameraReadyService.reviewSubmission(
                                conferenceId, submissionId, request, userId);
                return ResponseEntity.ok(response);
        }

        @PutMapping("/submissions/{submissionId}/current-version")
        @Operation(summary = "Chọn phiên bản chính thức cho bài nộp")
        public ResponseEntity<SubmissionDTO> setCurrentVersion(
                        @PathVariable Long conferenceId,
                        @PathVariable UUID submissionId,
                        @RequestBody Map<String, UUID> request,
                        @RequestHeader("X-User-Id") Long userId) {

                UUID versionId = request.get("versionId");
                SubmissionDTO submission = cameraReadyService.setCurrentVersion(
                                conferenceId, submissionId, versionId, userId);
                return ResponseEntity.ok(submission);
        }

        @GetMapping("/submissions/{submissionId}/metadata")
        @Operation(summary = "Lấy thông tin metadata của bài nộp")
        public ResponseEntity<MetadataDTO> getMetadata(
                        @PathVariable Long conferenceId,
                        @PathVariable UUID submissionId) {

                MetadataDTO metadata = cameraReadyService.getMetadata(submissionId);
                return ResponseEntity.ok(metadata);
        }

        @PutMapping("/submissions/{submissionId}/metadata")
        @Operation(summary = "Cập nhật metadata (DOI, số trang, v.v.)")
        public ResponseEntity<MetadataDTO> updateMetadata(
                        @PathVariable Long conferenceId,
                        @PathVariable UUID submissionId,
                        @Valid @RequestBody MetadataUpdateRequestDTO request,
                        @RequestHeader("X-User-Id") Long userId) {

                MetadataDTO metadata = cameraReadyService.updateMetadata(submissionId, request, userId);
                return ResponseEntity.ok(metadata);
        }

        @GetMapping("/statistics")
        @Operation(summary = "Thống kê tình hình nộp camera-ready")
        public ResponseEntity<StatisticsDTO> getStatistics(@PathVariable Long conferenceId) {
                StatisticsDTO statistics = cameraReadyService.getStatistics(conferenceId);
                return ResponseEntity.ok(statistics);
        }

        @GetMapping("/export/json")
        @Operation(summary = "Xuất dữ liệu kỷ yếu ra JSON")
        public ResponseEntity<ProceedingsExportDTO> exportJson(
                        @PathVariable Long conferenceId,
                        @RequestParam(required = false) Long trackId,
                        @RequestParam(defaultValue = "APPROVED") CameraReadyStatus status) {

                ProceedingsExportDTO export = cameraReadyService.exportProceedingsJson(conferenceId, trackId, status);
                return ResponseEntity.ok(export);
        }

        @GetMapping("/export/csv")
        @Operation(summary = "Xuất dữ liệu kỷ yếu ra CSV")
        public ResponseEntity<byte[]> exportCsv(
                        @PathVariable Long conferenceId,
                        @RequestParam(required = false) Long trackId,
                        @RequestParam(defaultValue = "APPROVED") CameraReadyStatus status) {

                byte[] csv = cameraReadyService.exportProceedingsCsv(conferenceId, trackId, status);
                String filename = String.format("proceedings_%s.csv", LocalDate.now());

                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType("text/csv"))
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                                .body(csv);
        }

        @GetMapping("/export/zip")
        @Operation(summary = "Xuất file ZIP chứa toàn bộ PDFs và metadata")
        public ResponseEntity<byte[]> exportZip(
                        @PathVariable Long conferenceId,
                        @RequestParam(required = false) Long trackId,
                        @RequestParam(defaultValue = "APPROVED") CameraReadyStatus status) {

                byte[] zip = cameraReadyService.exportProceedingsZip(conferenceId, trackId, status);
                String filename = String.format("proceedings_%s.zip", LocalDate.now());

                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType("application/zip"))
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                                .body(zip);
        }

        @GetMapping("/export/pdf")
        @Operation(summary = "Xuất file PDF tổng hợp (Proceedings)")
        public ResponseEntity<byte[]> exportPdf(
                        @PathVariable Long conferenceId,
                        @RequestParam(required = false) Long trackId,
                        @RequestParam(defaultValue = "APPROVED") CameraReadyStatus status) {

                byte[] pdf = cameraReadyService.exportProceedingsPdf(conferenceId, trackId, status);
                String filename = String.format("proceedings_%s.pdf", LocalDate.now());

                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType("application/pdf"))
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                                .body(pdf);
        }

        @PostMapping("/open")
        @Operation(summary = "Mở đợt nộp camera-ready cho hội nghị")
        public ResponseEntity<Map<String, Object>> openCameraReady(
                        @PathVariable Long conferenceId,
                        @RequestBody(required = false) OpenCameraReadyRequestDTO request,
                        @RequestHeader("X-User-Id") Long userId) {

                LocalDateTime deadline = request != null ? request.getDeadline() : null;
                int count = cameraReadyService.openCameraReady(conferenceId, deadline, userId);
                return ResponseEntity.ok(Map.of(
                                "conferenceId", conferenceId,
                                "status", "OPEN",
                                "papersInitialized", count));
        }

        @PostMapping("/close")
        @Operation(summary = "Đóng đợt nộp camera-ready")
        public ResponseEntity<Map<String, Object>> closeCameraReady(
                        @PathVariable Long conferenceId,
                        @RequestBody(required = false) Map<String, String> request,
                        @RequestHeader("X-User-Id") Long userId) {

                String reason = request != null ? request.get("reason") : null;
                cameraReadyService.closeCameraReady(conferenceId, reason, userId);

                return ResponseEntity.ok(Map.of(
                                "conferenceId", conferenceId,
                                "status", "CLOSED"));
        }
}
