package com.uth.confms.conference.controller;

import com.uth.confms.conference.dto.ApiResponse;
import com.uth.confms.conference.dto.SubmissionFormDTO;
import com.uth.confms.conference.service.SubmissionFormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conferences/{conferenceId}/submission-forms")
@RequiredArgsConstructor
@Tag(name = "Submission Form Management", description = "APIs for managing submission form configuration")
public class SubmissionFormController {
    
    private final SubmissionFormService formService;
    
    @PostMapping
    @Operation(summary = "Create a new form field", description = "Add a new field to the submission form")
    public ResponseEntity<ApiResponse<SubmissionFormDTO>> createForm(
            @PathVariable Long conferenceId,
            @Valid @RequestBody SubmissionFormDTO dto) {
        dto.setConferenceId(conferenceId);
        SubmissionFormDTO form = formService.createForm(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Form field created successfully", form));
    }
    
    @GetMapping
    @Operation(summary = "Get all form fields", description = "Retrieve all submission form fields for a conference")
    public ResponseEntity<ApiResponse<List<SubmissionFormDTO>>> getForms(@PathVariable Long conferenceId) {
        List<SubmissionFormDTO> forms = formService.getFormsByConferenceId(conferenceId);
        return ResponseEntity.ok(ApiResponse.success(forms));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update form field", description = "Update submission form field configuration")
    public ResponseEntity<ApiResponse<SubmissionFormDTO>> updateForm(
            @PathVariable Long id,
            @Valid @RequestBody SubmissionFormDTO dto) {
        SubmissionFormDTO form = formService.updateForm(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Form field updated successfully", form));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete form field", description = "Delete a submission form field")
    public ResponseEntity<ApiResponse<Void>> deleteForm(@PathVariable Long id) {
        formService.deleteForm(id);
        return ResponseEntity.ok(ApiResponse.success("Form field deleted successfully", null));
    }
}

