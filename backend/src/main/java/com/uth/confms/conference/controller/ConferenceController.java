package com.uth.confms.conference.controller;

import com.uth.confms.conference.dto.*;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.service.ConferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conferences")
@RequiredArgsConstructor
@Tag(name = "Conference Management", description = "APIs for managing conferences and CFP")
public class ConferenceController {
    
    private final ConferenceService conferenceService;
    
    @PostMapping
    @Operation(summary = "Create a new conference", description = "Create a new conference with CFP configuration")
    public ResponseEntity<ApiResponse<ConferenceDTO>> createConference(
            @Valid @RequestBody ConferenceCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {
        ConferenceDTO conference = conferenceService.createConference(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Conference created successfully", conference));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get conference by ID", description = "Retrieve conference details by ID")
    public ResponseEntity<ApiResponse<ConferenceDTO>> getConferenceById(@PathVariable Long id) {
        ConferenceDTO conference = conferenceService.getConferenceById(id);
        return ResponseEntity.ok(ApiResponse.success(conference));
    }
    
    @GetMapping
    @Operation(summary = "Get all conferences", description = "Retrieve all active conferences")
    public ResponseEntity<ApiResponse<List<ConferenceDTO>>> getAllConferences() {
        List<ConferenceDTO> conferences = conferenceService.getAllConferences();
        return ResponseEntity.ok(ApiResponse.success(conferences));
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get conferences by status", description = "Retrieve conferences filtered by status")
    public ResponseEntity<ApiResponse<List<ConferenceDTO>>> getConferencesByStatus(
            @PathVariable Conference.ConferenceStatus status) {
        List<ConferenceDTO> conferences = conferenceService.getConferencesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(conferences));
    }
    
    @GetMapping("/acronym/{acronym}")
    @Operation(summary = "Get conference by acronym", description = "Retrieve conference by acronym")
    public ResponseEntity<ApiResponse<ConferenceDTO>> getConferenceByAcronym(@PathVariable String acronym) {
        ConferenceDTO conference = conferenceService.getConferenceByAcronym(acronym);
        return ResponseEntity.ok(ApiResponse.success(conference));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update conference", description = "Update conference information")
    public ResponseEntity<ApiResponse<ConferenceDTO>> updateConference(
            @PathVariable Long id,
            @Valid @RequestBody ConferenceUpdateRequest request) {
        ConferenceDTO conference = conferenceService.updateConference(id, request);
        return ResponseEntity.ok(ApiResponse.success("Conference updated successfully", conference));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete conference", description = "Soft delete a conference")
    public ResponseEntity<ApiResponse<Void>> deleteConference(@PathVariable Long id) {
        conferenceService.deleteConference(id);
        return ResponseEntity.ok(ApiResponse.success("Conference deleted successfully", null));
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Change conference status", description = "Change the status of a conference")
    public ResponseEntity<ApiResponse<ConferenceDTO>> changeConferenceStatus(
            @PathVariable Long id,
            @RequestParam Conference.ConferenceStatus status) {
        ConferenceDTO conference = conferenceService.changeConferenceStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Conference status updated successfully", conference));
    }
}

