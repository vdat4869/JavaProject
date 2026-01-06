package com.uth.confms.conference.mapper;

import com.uth.confms.conference.dto.ConferenceTrackDTO;
import com.uth.confms.conference.entity.ConferenceTrack;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConferenceTrackMapper {
    
    @Mapping(target = "conferenceId", source = "conference.id")
    ConferenceTrackDTO toDTO(ConferenceTrack track);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conference", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ConferenceTrack toEntity(ConferenceTrackDTO dto);
}

