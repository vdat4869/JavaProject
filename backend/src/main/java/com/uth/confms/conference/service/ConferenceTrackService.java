package com.uth.confms.conference.service;

import com.uth.confms.conference.dto.ConferenceTrackDTO;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.entity.ConferenceTrack;
import com.uth.confms.conference.exception.ConferenceNotFoundException;
import com.uth.confms.conference.mapper.ConferenceTrackMapper;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.conference.repository.ConferenceTrackRepository;
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
public class ConferenceTrackService {
    
    private final ConferenceTrackRepository trackRepository;
    private final ConferenceRepository conferenceRepository;
    private final ConferenceTrackMapper trackMapper;
    
    public ConferenceTrackDTO createTrack(ConferenceTrackDTO dto) {
        log.info("Creating track: {} for conference: {}", dto.getName(), dto.getConferenceId());
        
        Conference conference = conferenceRepository.findByIdAndIsDeletedFalse(dto.getConferenceId())
                .orElseThrow(() -> new ConferenceNotFoundException("Conference not found with ID: " + dto.getConferenceId()));
        
        if (trackRepository.existsByConferenceIdAndName(dto.getConferenceId(), dto.getName())) {
            throw new IllegalArgumentException("Track with name '" + dto.getName() + "' already exists for this conference");
        }
        
        ConferenceTrack track = trackMapper.toEntity(dto);
        track.setConference(conference);
        if (track.getDisplayOrder() == null) {
            track.setDisplayOrder(0);
        }
        if (track.getIsActive() == null) {
            track.setIsActive(true);
        }
        
        ConferenceTrack saved = trackRepository.save(track);
        log.info("Track created successfully with ID: {}", saved.getId());
        return trackMapper.toDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public List<ConferenceTrackDTO> getTracksByConferenceId(Long conferenceId) {
        return trackRepository.findByConferenceIdOrderByDisplayOrderAsc(conferenceId).stream()
                .map(trackMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ConferenceTrackDTO> getActiveTracksByConferenceId(Long conferenceId) {
        return trackRepository.findByConferenceIdAndIsActiveTrueOrderByDisplayOrderAsc(conferenceId).stream()
                .map(trackMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public ConferenceTrackDTO updateTrack(Long id, ConferenceTrackDTO dto) {
        log.info("Updating track with ID: {}", id);
        
        ConferenceTrack track = trackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Track not found with ID: " + id));
        
        if (dto.getName() != null) {
            if (!dto.getName().equals(track.getName()) && 
                trackRepository.existsByConferenceIdAndName(track.getConference().getId(), dto.getName())) {
                throw new IllegalArgumentException("Track with name '" + dto.getName() + "' already exists");
            }
            track.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            track.setDescription(dto.getDescription());
        }
        if (dto.getDisplayOrder() != null) {
            track.setDisplayOrder(dto.getDisplayOrder());
        }
        if (dto.getIsActive() != null) {
            track.setIsActive(dto.getIsActive());
        }
        
        ConferenceTrack updated = trackRepository.save(track);
        log.info("Track updated successfully with ID: {}", updated.getId());
        return trackMapper.toDTO(updated);
    }
    
    public void deleteTrack(Long id) {
        log.info("Deleting track with ID: {}", id);
        trackRepository.deleteById(id);
        log.info("Track deleted successfully with ID: {}", id);
    }
}

