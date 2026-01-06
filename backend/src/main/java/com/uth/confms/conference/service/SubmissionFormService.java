package com.uth.confms.conference.service;

import com.uth.confms.conference.dto.SubmissionFormDTO;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.entity.SubmissionForm;
import com.uth.confms.conference.exception.ConferenceNotFoundException;
import com.uth.confms.conference.mapper.SubmissionFormMapper;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.conference.repository.SubmissionFormRepository;
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
public class SubmissionFormService {
    
    private final SubmissionFormRepository formRepository;
    private final ConferenceRepository conferenceRepository;
    private final SubmissionFormMapper formMapper;
    
    public SubmissionFormDTO createForm(SubmissionFormDTO dto) {
        log.info("Creating submission form field: {} for conference: {}", dto.getFieldName(), dto.getConferenceId());
        
        Conference conference = conferenceRepository.findByIdAndIsDeletedFalse(dto.getConferenceId())
                .orElseThrow(() -> new ConferenceNotFoundException("Conference not found with ID: " + dto.getConferenceId()));
        
        if (formRepository.existsByConferenceIdAndFieldName(dto.getConferenceId(), dto.getFieldName())) {
            throw new IllegalArgumentException("Form field with name '" + dto.getFieldName() + "' already exists for this conference");
        }
        
        SubmissionForm form = formMapper.toEntity(dto);
        form.setConference(conference);
        if (form.getDisplayOrder() == null) {
            form.setDisplayOrder(0);
        }
        if (form.getIsRequired() == null) {
            form.setIsRequired(false);
        }
        
        SubmissionForm saved = formRepository.save(form);
        log.info("Submission form field created successfully with ID: {}", saved.getId());
        return formMapper.toDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public List<SubmissionFormDTO> getFormsByConferenceId(Long conferenceId) {
        return formRepository.findByConferenceIdOrderByDisplayOrderAsc(conferenceId).stream()
                .map(formMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public SubmissionFormDTO updateForm(Long id, SubmissionFormDTO dto) {
        log.info("Updating submission form field with ID: {}", id);
        
        SubmissionForm form = formRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Form field not found with ID: " + id));
        
        if (dto.getFieldLabel() != null) {
            form.setFieldLabel(dto.getFieldLabel());
        }
        if (dto.getFieldType() != null) {
            form.setFieldType(dto.getFieldType());
        }
        if (dto.getFieldOptions() != null) {
            form.setFieldOptions(dto.getFieldOptions());
        }
        if (dto.getIsRequired() != null) {
            form.setIsRequired(dto.getIsRequired());
        }
        if (dto.getDisplayOrder() != null) {
            form.setDisplayOrder(dto.getDisplayOrder());
        }
        if (dto.getValidationRules() != null) {
            form.setValidationRules(dto.getValidationRules());
        }
        
        SubmissionForm updated = formRepository.save(form);
        log.info("Submission form field updated successfully with ID: {}", updated.getId());
        return formMapper.toDTO(updated);
    }
    
    public void deleteForm(Long id) {
        log.info("Deleting submission form field with ID: {}", id);
        formRepository.deleteById(id);
        log.info("Submission form field deleted successfully with ID: {}", id);
    }
}

