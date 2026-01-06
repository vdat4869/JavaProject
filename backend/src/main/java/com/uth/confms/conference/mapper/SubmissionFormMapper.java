package com.uth.confms.conference.mapper;

import com.uth.confms.conference.dto.SubmissionFormDTO;
import com.uth.confms.conference.entity.SubmissionForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubmissionFormMapper {
    
    @Mapping(target = "conferenceId", source = "conference.id")
    SubmissionFormDTO toDTO(SubmissionForm form);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conference", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SubmissionForm toEntity(SubmissionFormDTO dto);
}

