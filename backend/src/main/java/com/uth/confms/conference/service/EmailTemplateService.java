package com.uth.confms.conference.service;

import com.uth.confms.conference.dto.EmailTemplateDTO;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.entity.EmailTemplate;
import com.uth.confms.conference.exception.ConferenceNotFoundException;
import com.uth.confms.conference.mapper.EmailTemplateMapper;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.conference.repository.EmailTemplateRepository;
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
public class EmailTemplateService {
    
    private final EmailTemplateRepository templateRepository;
    private final ConferenceRepository conferenceRepository;
    private final EmailTemplateMapper templateMapper;
    
    public EmailTemplateDTO createTemplate(EmailTemplateDTO dto) {
        log.info("Creating email template: {} for conference: {}", dto.getTemplateType(), dto.getConferenceId());
        
        Conference conference = conferenceRepository.findByIdAndIsDeletedFalse(dto.getConferenceId())
                .orElseThrow(() -> new ConferenceNotFoundException("Conference not found with ID: " + dto.getConferenceId()));
        
        if (templateRepository.existsByConferenceIdAndTemplateType(dto.getConferenceId(), dto.getTemplateType())) {
            throw new IllegalArgumentException("Email template with type '" + dto.getTemplateType() + "' already exists for this conference");
        }
        
        EmailTemplate template = templateMapper.toEntity(dto);
        template.setConference(conference);
        if (template.getIsActive() == null) {
            template.setIsActive(true);
        }
        
        EmailTemplate saved = templateRepository.save(template);
        log.info("Email template created successfully with ID: {}", saved.getId());
        return templateMapper.toDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public List<EmailTemplateDTO> getTemplatesByConferenceId(Long conferenceId) {
        return templateRepository.findByConferenceId(conferenceId).stream()
                .map(templateMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public EmailTemplateDTO getTemplateByType(Long conferenceId, EmailTemplate.TemplateType templateType) {
        EmailTemplate template = templateRepository.findByConferenceIdAndTemplateType(conferenceId, templateType)
                .orElseThrow(() -> new IllegalArgumentException("Email template not found with type: " + templateType));
        return templateMapper.toDTO(template);
    }
    
    public EmailTemplateDTO updateTemplate(Long id, EmailTemplateDTO dto) {
        log.info("Updating email template with ID: {}", id);
        
        EmailTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Email template not found with ID: " + id));
        
        if (dto.getSubject() != null) {
            template.setSubject(dto.getSubject());
        }
        if (dto.getBody() != null) {
            template.setBody(dto.getBody());
        }
        if (dto.getVariables() != null) {
            template.setVariables(dto.getVariables());
        }
        if (dto.getIsActive() != null) {
            template.setIsActive(dto.getIsActive());
        }
        
        EmailTemplate updated = templateRepository.save(template);
        log.info("Email template updated successfully with ID: {}", updated.getId());
        return templateMapper.toDTO(updated);
    }
    
    public EmailTemplateDTO upsertTemplate(EmailTemplateDTO dto) {
        log.info("Upserting email template: {} for conference: {}", dto.getTemplateType(), dto.getConferenceId());
        
        Conference conference = conferenceRepository.findByIdAndIsDeletedFalse(dto.getConferenceId())
                .orElseThrow(() -> new ConferenceNotFoundException("Conference not found with ID: " + dto.getConferenceId()));
        
        EmailTemplate template = templateRepository.findByConferenceIdAndTemplateType(dto.getConferenceId(), dto.getTemplateType())
                .orElse(null);
        
        if (template == null) {
            template = templateMapper.toEntity(dto);
            template.setConference(conference);
            if (template.getIsActive() == null) {
                template.setIsActive(true);
            }
        } else {
            if (dto.getSubject() != null) {
                template.setSubject(dto.getSubject());
            }
            if (dto.getBody() != null) {
                template.setBody(dto.getBody());
            }
            if (dto.getVariables() != null) {
                template.setVariables(dto.getVariables());
            }
            if (dto.getIsActive() != null) {
                template.setIsActive(dto.getIsActive());
            }
        }
        
        EmailTemplate saved = templateRepository.save(template);
        log.info("Email template upserted successfully with ID: {}", saved.getId());
        return templateMapper.toDTO(saved);
    }
    
    public void deleteTemplate(Long id) {
        log.info("Deleting email template with ID: {}", id);
        templateRepository.deleteById(id);
        log.info("Email template deleted successfully with ID: {}", id);
    }
}

