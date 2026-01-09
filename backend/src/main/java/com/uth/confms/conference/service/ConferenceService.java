package com.uth.confms.conference.service;

import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.conference.dto.ConferenceCreateDTO;
import com.uth.confms.conference.dto.ConferenceResponseDTO;
import com.uth.confms.conference.dto.ConferenceUpdateDTO;
import com.uth.confms.conference.dto.DeadlineDTO;
import com.uth.confms.conference.dto.TrackDTO;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.entity.Deadline;
import com.uth.confms.conference.entity.Track;
import com.uth.confms.conference.repository.ConferenceRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý hội nghị (Conference) và CFP
 *
 * <p>Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 *   <li>Tạo, cập nhật, xóa hội nghị
 *   <li>Quản lý tracks và deadlines
 *   <li>Publish/unpublish hội nghị
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class ConferenceService {
  private final ConferenceRepository conferenceRepository;

  public ConferenceService(ConferenceRepository conferenceRepository) {
    this.conferenceRepository = conferenceRepository;
  }

  /**
   * Tạo hội nghị mới
   *
   * @param dto Thông tin hội nghị cần tạo (name, acronym, description, tracks, deadlines)
   * @param chairId ID của chair tạo hội nghị
   * @return ConferenceResponseDTO chứa thông tin hội nghị đã tạo
   */
  @Transactional
  public ConferenceResponseDTO createConference(ConferenceCreateDTO dto, Long chairId) {
    Conference conference =
        Conference.builder()
            .name(dto.getName())
            .acronym(dto.getAcronym())
            .description(dto.getDescription())
            .chairId(chairId)
            .published(false)
            .build();

    Conference savedConference = conferenceRepository.save(conference);
    final Conference finalConference = savedConference;

    // Add tracks if provided
    if (dto.getTracks() != null) {
      List<Track> tracks =
          dto.getTracks().stream()
              .map(
                  trackDTO ->
                      Track.builder()
                          .conference(finalConference)
                          .name(trackDTO.getName())
                          .description(trackDTO.getDescription())
                          .active(trackDTO.getActive() != null ? trackDTO.getActive() : true)
                          .build())
              .collect(Collectors.toList());
      finalConference.setTracks(tracks);
    }

    // Add deadlines if provided
    if (dto.getDeadlines() != null) {
      List<Deadline> deadlines =
          dto.getDeadlines().stream()
              .map(
                  deadlineDTO ->
                      Deadline.builder()
                          .conference(finalConference)
                          .type(Deadline.DeadlineType.valueOf(deadlineDTO.getType()))
                          .dueDate(deadlineDTO.getDueDate())
                          .description(deadlineDTO.getDescription())
                          .hardDeadline(
                              deadlineDTO.getHardDeadline() != null
                                  ? deadlineDTO.getHardDeadline()
                                  : true)
                          .build())
              .collect(Collectors.toList());
      finalConference.setDeadlines(deadlines);
    }

    Conference result = conferenceRepository.save(finalConference);
    return mapToDTO(result);
  }

  /**
   * Lấy thông tin hội nghị theo ID
   *
   * @param id ID của hội nghị
   * @return ConferenceResponseDTO chứa thông tin hội nghị
   * @throws NotFoundException Nếu không tìm thấy hội nghị
   */
  public ConferenceResponseDTO getConference(Long id) {
    String errorMessage = "Conference with id " + id + " not found";
    Conference conference =
        conferenceRepository.findById(id).orElseThrow(() -> new NotFoundException(errorMessage));
    return mapToDTO(conference);
  }

  public List<ConferenceResponseDTO> getPublishedConferences() {
    return conferenceRepository.findByPublishedTrue().stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  public List<ConferenceResponseDTO> getConferencesByChair(Long chairId) {
    return conferenceRepository.findByChairId(chairId).stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  @Transactional
  public ConferenceResponseDTO updateConference(Long id, ConferenceUpdateDTO dto, Long chairId) {
    String errorMessage = "Conference with id " + id + " not found";
    Conference conference =
        conferenceRepository.findById(id).orElseThrow(() -> new NotFoundException(errorMessage));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can update this conference");
    }

    if (dto.getName() != null) {
      conference.setName(dto.getName());
    }
    if (dto.getAcronym() != null) {
      conference.setAcronym(dto.getAcronym());
    }
    if (dto.getDescription() != null) {
      conference.setDescription(dto.getDescription());
    }
    if (dto.getPublished() != null) {
      conference.setPublished(dto.getPublished());
    }

    // Update tracks if provided
    if (dto.getTracks() != null) {
      conference.getTracks().clear();
      final Conference finalConferenceForTracks = conference;
      List<Track> tracks =
          dto.getTracks().stream()
              .map(
                  trackDTO ->
                      Track.builder()
                          .conference(finalConferenceForTracks)
                          .name(trackDTO.getName())
                          .description(trackDTO.getDescription())
                          .active(trackDTO.getActive() != null ? trackDTO.getActive() : true)
                          .build())
              .collect(Collectors.toList());
      conference.setTracks(tracks);
    }

    // Update deadlines if provided
    if (dto.getDeadlines() != null) {
      conference.getDeadlines().clear();
      final Conference finalConferenceForDeadlines = conference;
      List<Deadline> deadlines =
          dto.getDeadlines().stream()
              .map(
                  deadlineDTO ->
                      Deadline.builder()
                          .conference(finalConferenceForDeadlines)
                          .type(Deadline.DeadlineType.valueOf(deadlineDTO.getType()))
                          .dueDate(deadlineDTO.getDueDate())
                          .description(deadlineDTO.getDescription())
                          .hardDeadline(
                              deadlineDTO.getHardDeadline() != null
                                  ? deadlineDTO.getHardDeadline()
                                  : true)
                          .build())
              .collect(Collectors.toList());
      conference.setDeadlines(deadlines);
    }

    Conference updatedConference = conferenceRepository.save(conference);
    return mapToDTO(updatedConference);
  }

  @Transactional
  public void deleteConference(Long id, Long chairId) {
    String errorMessage = "Conference with id " + id + " not found";
    Conference conference =
        conferenceRepository.findById(id).orElseThrow(() -> new NotFoundException(errorMessage));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can delete this conference");
    }

    conferenceRepository.delete(conference);
  }

  private ConferenceResponseDTO mapToDTO(Conference conference) {
    List<TrackDTO> tracks =
        conference.getTracks().stream()
            .map(
                track ->
                    TrackDTO.builder()
                        .id(track.getId())
                        .name(track.getName())
                        .description(track.getDescription())
                        .active(track.getActive())
                        .build())
            .collect(Collectors.toList());

    List<DeadlineDTO> deadlines =
        conference.getDeadlines().stream()
            .map(
                deadline ->
                    DeadlineDTO.builder()
                        .id(deadline.getId())
                        .type(deadline.getType().name())
                        .dueDate(deadline.getDueDate())
                        .description(deadline.getDescription())
                        .hardDeadline(deadline.getHardDeadline())
                        .build())
            .collect(Collectors.toList());

    return ConferenceResponseDTO.builder()
        .id(conference.getId())
        .name(conference.getName())
        .acronym(conference.getAcronym())
        .description(conference.getDescription())
        .chairId(conference.getChairId())
        .published(conference.getPublished())
        .tracks(tracks)
        .deadlines(deadlines)
        .createdAt(conference.getCreatedAt())
        .updatedAt(conference.getUpdatedAt())
        .build();
  }
}
