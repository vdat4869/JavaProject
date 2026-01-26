package com.uth.confms.decision.controller;

import com.uth.confms.common.dto.ApiResponse;
import com.uth.confms.decision.entity.NotificationLog;
import com.uth.confms.decision.repository.NotificationLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller để quản lý và theo dõi nhật ký thông báo
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "API quản lý và theo dõi nhật ký thông báo")
public class NotificationController {

    private final NotificationLogRepository notificationLogRepository;

    @Operation(summary = "Lấy tất cả nhật ký thông báo (Admin/Chair)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('CHAIR')")
    public ResponseEntity<ApiResponse<List<NotificationLog>>> getAllLogs() {
        return ResponseEntity.ok(ApiResponse.success(notificationLogRepository.findAll()));
    }

    @Operation(summary = "Lấy nhật ký thông báo theo submission")
    @GetMapping("/submission/{submissionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CHAIR')")
    public ResponseEntity<ApiResponse<List<NotificationLog>>> getLogsBySubmission(@PathVariable Long submissionId) {
        return ResponseEntity.ok(ApiResponse.success(notificationLogRepository.findBySubmissionId(submissionId)));
    }

    @Operation(summary = "Lấy nhật ký thông báo theo người nhận")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CHAIR')")
    public ResponseEntity<ApiResponse<List<NotificationLog>>> getLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(notificationLogRepository.findByUserId(userId)));
    }
}
