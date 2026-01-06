package com.uth.confms.conference.repository;

import com.uth.confms.conference.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    
    List<EmailTemplate> findByConferenceId(Long conferenceId);
    
    Optional<EmailTemplate> findByConferenceIdAndTemplateType(Long conferenceId, EmailTemplate.TemplateType templateType);
    
    boolean existsByConferenceIdAndTemplateType(Long conferenceId, EmailTemplate.TemplateType templateType);
}

