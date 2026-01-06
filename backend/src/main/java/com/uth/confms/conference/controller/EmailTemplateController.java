package com.uth.confms.conference.controller;

import com.uth.confms.conference.dto.ApiResponse;
import com.uth.confms.conference.dto.EmailTemplateDTO;
import com.uth.confms.conference.entity.EmailTemplate;
import com.uth.confms.conference.service.EmailTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conferences/{conferenceId}/email-templates")
@RequiredArgsConstructor
@Tag(name = "Email Template Management", description = "APIs for managing email templates")
public class EmailTemplateController {
    
    private final EmailTemplateService templateService;
    
    @PostMapping
    @Operation(summary = "Create a new email template", description = "Create a new email template for a conference")
    public ResponseEntity<ApiResponse<EmailTemplateDTO>> createTemplate(
            @PathVariable Long conferenceId,
            @Valid @RequestBody EmailTemplateDTO dto) {
        dto.setConferenceId(conferenceId);
        EmailTemplateDTO template = templateService.createTemplate(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Email template created successfully", template));
    }
    
    @GetMapping
    @Operation(summary = "Get all email templates", description = "Retrieve all email templates for a conference")
    public ResponseEntity<ApiResponse<List<EmailTemplateDTO>>> getTemplates(@PathVariable Long conferenceId) {
        List<EmailTemplateDTO> templates = templateService.getTemplatesByConferenceId(conferenceId);
        return ResponseEntity.ok(ApiResponse.success(templates));
    }
    
    @GetMapping("/type/{templateType}")
    @Operation(summary = "Get template by type", description = "Retrieve email template by type")
    public ResponseEntity<ApiResponse<EmailTemplateDTO>> getTemplateByType(
            @PathVariable Long conferenceId,
            @PathVariable EmailTemplate.TemplateType templateType) {
        EmailTemplateDTO template = templateService.getTemplateByType(conferenceId, templateType);
        return ResponseEntity.ok(ApiResponse.success(template));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update email template", description = "Update email template content")
    public ResponseEntity<ApiResponse<EmailTemplateDTO>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody EmailTemplateDTO dto) {
        EmailTemplateDTO template = templateService.updateTemplate(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Email template updated successfully", template));
    }
    
    @PutMapping("/upsert")
    @Operation(summary = "Upsert email template", description = "Create or update email template by type")
    public ResponseEntity<ApiResponse<EmailTemplateDTO>> upsertTemplate(
            @PathVariable Long conferenceId,
            @Valid @RequestBody EmailTemplateDTO dto) {
        dto.setConferenceId(conferenceId);
        EmailTemplateDTO template = templateService.upsertTemplate(dto);
        return ResponseEntity.ok(ApiResponse.success("Email template upserted successfully", template));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete email template", description = "Delete an email template")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Email template deleted successfully", null));
    }
}

