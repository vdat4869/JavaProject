package com.uth.confms.conference.controller;

import com.uth.confms.conference.dto.ApiResponse;
import com.uth.confms.conference.dto.ConferenceTrackDTO;
import com.uth.confms.conference.service.ConferenceTrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conferences/{conferenceId}/tracks")
@RequiredArgsConstructor
@Tag(name = "Conference Track Management", description = "APIs for managing conference tracks/topics")
public class ConferenceTrackController {
    
    private final ConferenceTrackService trackService;
    
    @PostMapping
    @Operation(summary = "Create a new track", description = "Create a new track/topic for a conference")
    public ResponseEntity<ApiResponse<ConferenceTrackDTO>> createTrack(
            @PathVariable Long conferenceId,
            @Valid @RequestBody ConferenceTrackDTO dto) {
        dto.setConferenceId(conferenceId);
        ConferenceTrackDTO track = trackService.createTrack(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Track created successfully", track));
    }
    
    @GetMapping
    @Operation(summary = "Get all tracks", description = "Retrieve all tracks for a conference")
    public ResponseEntity<ApiResponse<List<ConferenceTrackDTO>>> getTracks(@PathVariable Long conferenceId) {
        List<ConferenceTrackDTO> tracks = trackService.getTracksByConferenceId(conferenceId);
        return ResponseEntity.ok(ApiResponse.success(tracks));
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get active tracks", description = "Retrieve only active tracks for a conference")
    public ResponseEntity<ApiResponse<List<ConferenceTrackDTO>>> getActiveTracks(@PathVariable Long conferenceId) {
        List<ConferenceTrackDTO> tracks = trackService.getActiveTracksByConferenceId(conferenceId);
        return ResponseEntity.ok(ApiResponse.success(tracks));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update track", description = "Update track information")
    public ResponseEntity<ApiResponse<ConferenceTrackDTO>> updateTrack(
            @PathVariable Long id,
            @Valid @RequestBody ConferenceTrackDTO dto) {
        ConferenceTrackDTO track = trackService.updateTrack(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Track updated successfully", track));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete track", description = "Delete a track")
    public ResponseEntity<ApiResponse<Void>> deleteTrack(@PathVariable Long id) {
        trackService.deleteTrack(id);
        return ResponseEntity.ok(ApiResponse.success("Track deleted successfully", null));
    }
}

