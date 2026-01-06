package com.uth.confms.conference.controller;

import com.uth.confms.conference.dto.ApiResponse;
import com.uth.confms.conference.dto.ConferenceDTO;
import com.uth.confms.conference.dto.ConferenceTrackDTO;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.service.ConferenceService;
import com.uth.confms.conference.service.ConferenceTrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/cfp")
@RequiredArgsConstructor
@Tag(name = "Public CFP", description = "Public APIs for viewing CFP information")
public class PublicCFPController {
    
    private final ConferenceService conferenceService;
    private final ConferenceTrackService trackService;
    
    @GetMapping("/conferences")
    @Operation(summary = "Get open conferences", description = "Retrieve all open conferences for public viewing")
    public ResponseEntity<ApiResponse<List<ConferenceDTO>>> getOpenConferences() {
        List<ConferenceDTO> conferences = conferenceService.getConferencesByStatus(Conference.ConferenceStatus.OPEN);
        return ResponseEntity.ok(ApiResponse.success(conferences));
    }
    
    @GetMapping("/conferences/{acronym}")
    @Operation(summary = "Get conference CFP by acronym", description = "Retrieve CFP information for a specific conference")
    public ResponseEntity<ApiResponse<ConferenceDTO>> getConferenceCFP(@PathVariable String acronym) {
        ConferenceDTO conference = conferenceService.getConferenceByAcronym(acronym);
        return ResponseEntity.ok(ApiResponse.success(conference));
    }
    
    @GetMapping("/conferences/{conferenceId}/tracks")
    @Operation(summary = "Get conference tracks", description = "Retrieve active tracks for a conference")
    public ResponseEntity<ApiResponse<List<ConferenceTrackDTO>>> getConferenceTracks(@PathVariable Long conferenceId) {
        List<ConferenceTrackDTO> tracks = trackService.getActiveTracksByConferenceId(conferenceId);
        return ResponseEntity.ok(ApiResponse.success(tracks));
    }
}

