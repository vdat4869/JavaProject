package com.uth.confms.conference.mapper;

import com.uth.confms.conference.dto.EmailTemplateDTO;
import com.uth.confms.conference.entity.EmailTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmailTemplateMapper {
    
    @Mapping(target = "conferenceId", source = "conference.id")
    EmailTemplateDTO toDTO(EmailTemplate template);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conference", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EmailTemplate toEntity(EmailTemplateDTO dto);
}

