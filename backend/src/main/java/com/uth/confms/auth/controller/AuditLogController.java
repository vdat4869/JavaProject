package com.uth.confms.auth.controller;

import com.uth.confms.auth.dto.AuditLogDTO;
import com.uth.confms.auth.entity.AuditLog;
import com.uth.confms.auth.repository.AuditLogRepository;
import com.uth.confms.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller quản lý audit logs
 *
 * <p>
 * Các endpoints:
 *
 * <ul>
 * <li>GET /api/audit-logs - List audit logs với filters và pagination (ADMIN
 * only)
 * <li>GET /api/audit-logs/{id} - Get specific audit log (ADMIN only)
 * <li>GET /api/audit-logs/export - Export audit logs (ADMIN only)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuditLogDTO>>> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.debug(
                "Querying audit logs with filters: userId={}, action={}, resource={}, resourceId={}, startDate={}, endDate={}, page={}, size={}",
                userId, action, resource, resourceId, startDate, endDate, page, size);

        // Parse dates if provided
        java.time.LocalDateTime startDateTime = null;
        java.time.LocalDateTime endDateTime = null;
        if (startDate != null && !startDate.isEmpty()) {
            try {
                startDateTime = java.time.LocalDateTime.parse(startDate);
            } catch (Exception e) {
                log.warn("Invalid startDate format: {}", startDate);
            }
        }
        if (endDate != null && !endDate.isEmpty()) {
            try {
                endDateTime = java.time.LocalDateTime.parse(endDate);
            } catch (Exception e) {
                log.warn("Invalid endDate format: {}", endDate);
            }
        }

        // Create pageable
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Query with filters
        org.springframework.data.jpa.domain.Specification<AuditLog> spec = com.uth.confms.auth.repository.AuditLogSpecification
                .withFilters(
                        userId, action, resource, resourceId, startDateTime, endDateTime);

        Page<AuditLog> auditLogs = auditLogRepository.findAll(spec, pageable);

        // Map to DTOs
        Page<AuditLogDTO> dtoPage = auditLogs.map(this::mapToDTO);

        return ResponseEntity.ok(ApiResponse.success(dtoPage));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuditLogDTO>> getAuditLog(@PathVariable Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new com.uth.confms.common.exception.NotFoundException("Audit log not found"));

        return ResponseEntity.ok(ApiResponse.success(mapToDTO(auditLog)));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "CSV") String format) {

        log.info(
                "Exporting audit logs with filters: userId={}, action={}, resource={}, resourceId={}, startDate={}, endDate={}, format={}",
                userId, action, resource, resourceId, startDate, endDate, format);

        // Parse dates if provided
        java.time.LocalDateTime startDateTime = null;
        java.time.LocalDateTime endDateTime = null;
        if (startDate != null && !startDate.isEmpty()) {
            try {
                startDateTime = java.time.LocalDateTime.parse(startDate);
            } catch (Exception e) {
                log.warn("Invalid startDate format: {}", startDate);
            }
        }
        if (endDate != null && !endDate.isEmpty()) {
            try {
                endDateTime = java.time.LocalDateTime.parse(endDate);
            } catch (Exception e) {
                log.warn("Invalid endDate format: {}", endDate);
            }
        }

        // Get all matching logs (no pagination for export)
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "timestamp"));

        org.springframework.data.jpa.domain.Specification<AuditLog> spec = com.uth.confms.auth.repository.AuditLogSpecification
                .withFilters(
                        userId, action, resource, resourceId, startDateTime, endDateTime);

        Page<AuditLog> auditLogs = auditLogRepository.findAll(spec, pageable);

        byte[] exportData;
        String filename;
        String contentType;

        if ("CSV".equalsIgnoreCase(format)) {
            exportData = exportToCsv(auditLogs.getContent());
            filename = String.format("audit-logs_%s.csv", java.time.LocalDate.now());
            contentType = "text/csv";
        } else {
            // Default to CSV
            exportData = exportToCsv(auditLogs.getContent());
            filename = String.format("audit-logs_%s.csv", java.time.LocalDate.now());
            contentType = "text/csv";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(exportData);
    }

    private AuditLogDTO mapToDTO(AuditLog auditLog) {
        return AuditLogDTO.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .username(auditLog.getUsername())
                .action(auditLog.getAction())
                .resource(auditLog.getResource())
                .resourceId(auditLog.getResourceId())
                .details(auditLog.getDetails())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .timestamp(auditLog.getTimestamp())
                .build();
    }

    private byte[] exportToCsv(List<AuditLog> auditLogs) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(baos, true, StandardCharsets.UTF_8)) {

            // Write CSV header
            writer.println("id,user_id,username,action,resource,resource_id,details,ip_address,user_agent,timestamp");

            // Write data rows
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (AuditLog log : auditLogs) {
                writer.printf("%d,%d,\"%s\",\"%s\",\"%s\",%s,\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        log.getId(),
                        log.getUserId(),
                        escapeCsv(log.getUsername()),
                        escapeCsv(log.getAction()),
                        escapeCsv(log.getResource()),
                        log.getResourceId() != null ? log.getResourceId() : "",
                        escapeCsv(log.getDetails()),
                        escapeCsv(log.getIpAddress()),
                        escapeCsv(log.getUserAgent()),
                        log.getTimestamp() != null ? log.getTimestamp().format(formatter) : "");
            }

            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error exporting audit logs to CSV", e);
            throw new com.uth.confms.common.exception.BusinessException(
                    "Failed to export audit logs: " + e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null)
            return "";
        return value.replace("\"", "\"\"");
    }
}
