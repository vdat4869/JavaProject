package com.uth.confms.conference.repository;

import com.uth.confms.conference.entity.ConferenceTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConferenceTrackRepository extends JpaRepository<ConferenceTrack, Long> {
    
    List<ConferenceTrack> findByConferenceIdOrderByDisplayOrderAsc(Long conferenceId);
    
    List<ConferenceTrack> findByConferenceIdAndIsActiveTrueOrderByDisplayOrderAsc(Long conferenceId);
    
    boolean existsByConferenceIdAndName(Long conferenceId, String name);
}

