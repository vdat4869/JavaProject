package com.uth.confms.conference.repository;

import com.uth.confms.conference.entity.SubmissionForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionFormRepository extends JpaRepository<SubmissionForm, Long> {
    
    List<SubmissionForm> findByConferenceIdOrderByDisplayOrderAsc(Long conferenceId);
    
    boolean existsByConferenceIdAndFieldName(Long conferenceId, String fieldName);
}

