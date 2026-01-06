package com.uth.confms.conference.service;

import com.uth.confms.conference.dto.*;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.exception.ConferenceNotFoundException;
import com.uth.confms.conference.mapper.ConferenceMapper;
import com.uth.confms.conference.repository.ConferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConferenceService {
    
    private final ConferenceRepository conferenceRepository;
    private final ConferenceMapper conferenceMapper;
    
    public ConferenceDTO createConference(ConferenceCreateRequest request, Long createdBy) {
        log.info("Creating conference: {}", request.getName());
        
        // Check if acronym already exists
        if (request.getAcronym() != null && !request.getAcronym().isEmpty()) {
            if (conferenceRepository.existsByAcronymAndIsDeletedFalse(request.getAcronym())) {
                throw new IllegalArgumentException("Conference acronym already exists: " + request.getAcronym());
            }
        }
        
        Conference conference = Conference.builder()
                .name(request.getName())
                .description(request.getDescription())
                .acronym(request.getAcronym())
                .year(request.getYear())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .submissionDeadline(request.getSubmissionDeadline())
                .reviewDeadline(request.getReviewDeadline())
                .cameraReadyDeadline(request.getCameraReadyDeadline())
                .reviewMode(request.getReviewMode() != null ? request.getReviewMode() : Conference.ReviewMode.DOUBLE_BLIND)
                .status(Conference.ConferenceStatus.DRAFT)
                .createdBy(createdBy)
                .isDeleted(false)
                .build();
        
        Conference saved = conferenceRepository.save(conference);
        log.info("Conference created successfully with ID: {}", saved.getId());
        return conferenceMapper.toDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public ConferenceDTO getConferenceById(Long id) {
        Conference conference = conferenceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ConferenceNotFoundException("Conference not found with ID: " + id));
        return conferenceMapper.toDTO(conference);
    }
    
    @Transactional(readOnly = true)
    public List<ConferenceDTO> getAllConferences() {
        return conferenceRepository.findByIsDeletedFalse().stream()
                .map(conferenceMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ConferenceDTO> getConferencesByStatus(Conference.ConferenceStatus status) {
        return conferenceRepository.findByStatusAndIsDeletedFalse(status).stream()
                .map(conferenceMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ConferenceDTO getConferenceByAcronym(String acronym) {
        Conference conference = conferenceRepository.findByAcronymAndIsDeletedFalse(acronym)
                .orElseThrow(() -> new ConferenceNotFoundException("Conference not found with acronym: " + acronym));
        return conferenceMapper.toDTO(conference);
    }
    
    public ConferenceDTO updateConference(Long id, ConferenceUpdateRequest request) {
        log.info("Updating conference with ID: {}", id);
        
        Conference conference = conferenceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ConferenceNotFoundException("Conference not found with ID: " + id));
        
        // Update fields if provided
        if (request.getName() != null) {
            conference.setName(request.getName());
        }
        if (request.getDescription() != null) {
            conference.setDescription(request.getDescription());
        }
        if (request.getAcronym() != null && !request.getAcronym().equals(conference.getAcronym())) {
            if (conferenceRepository.existsByAcronymAndIsDeletedFalse(request.getAcronym())) {
                throw new IllegalArgumentException("Conference acronym already exists: " + request.getAcronym());
            }
            conference.setAcronym(request.getAcronym());
        }
        if (request.getYear() != null) {
            conference.setYear(request.getYear());
        }
        if (request.getStartDate() != null) {
            conference.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            conference.setEndDate(request.getEndDate());
        }
        if (request.getSubmissionDeadline() != null) {
            conference.setSubmissionDeadline(request.getSubmissionDeadline());
        }
        if (request.getReviewDeadline() != null) {
            conference.setReviewDeadline(request.getReviewDeadline());
        }
        if (request.getCameraReadyDeadline() != null) {
            conference.setCameraReadyDeadline(request.getCameraReadyDeadline());
        }
        if (request.getReviewMode() != null) {
            conference.setReviewMode(request.getReviewMode());
        }
        if (request.getStatus() != null) {
            conference.setStatus(request.getStatus());
        }
        
        Conference updated = conferenceRepository.save(conference);
        log.info("Conference updated successfully with ID: {}", updated.getId());
        return conferenceMapper.toDTO(updated);
    }
    
    public void deleteConference(Long id) {
        log.info("Soft deleting conference with ID: {}", id);
        
        Conference conference = conferenceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ConferenceNotFoundException("Conference not found with ID: " + id));
        
        conference.setIsDeleted(true);
        conferenceRepository.save(conference);
        log.info("Conference soft deleted successfully with ID: {}", id);
    }
    
    public ConferenceDTO changeConferenceStatus(Long id, Conference.ConferenceStatus status) {
        log.info("Changing conference status to {} for ID: {}", status, id);
        
        Conference conference = conferenceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ConferenceNotFoundException("Conference not found with ID: " + id));
        
        conference.setStatus(status);
        Conference updated = conferenceRepository.save(conference);
        log.info("Conference status changed successfully for ID: {}", id);
        return conferenceMapper.toDTO(updated);
    }
}

