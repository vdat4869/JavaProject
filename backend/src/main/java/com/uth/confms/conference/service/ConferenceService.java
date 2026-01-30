package com.uth.confms.conference.service;

import com.uth.confms.auth.entity.Role;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.enums.RoleName;
import com.uth.confms.auth.repository.RoleRepository;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.conference.dto.ConferenceCreateDTO;
import com.uth.confms.conference.dto.ConferenceResponseDTO;
import com.uth.confms.conference.dto.ConferenceUpdateDTO;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.conference.dto.DeadlineDTO;
import com.uth.confms.conference.dto.KeywordDTO;
import com.uth.confms.conference.dto.TopicDTO;
import com.uth.confms.conference.dto.TrackDTO;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.entity.Deadline;
import com.uth.confms.conference.entity.Keyword;
import com.uth.confms.conference.entity.Topic;
import com.uth.confms.conference.entity.Track;
import com.uth.confms.conference.dto.CFPResponseDTO;
import com.uth.confms.conference.entity.CFP;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.conference.repository.KeywordRepository;
import com.uth.confms.submission.repository.SubmissionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý hội nghị (Conference) và CFP
 *
 * <p>
 * Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 * <li>Tạo, cập nhật, xóa hội nghị
 * <li>Quản lý tracks và deadlines
 * <li>Publish/unpublish hội nghị
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class ConferenceService {
  private final ConferenceRepository conferenceRepository;
  private final KeywordRepository keywordRepository;
  private final SubmissionRepository submissionRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  public ConferenceService(
      ConferenceRepository conferenceRepository,
      KeywordRepository keywordRepository,
      SubmissionRepository submissionRepository,
      UserRepository userRepository,
      RoleRepository roleRepository) {
    this.conferenceRepository = conferenceRepository;
    this.keywordRepository = keywordRepository;
    this.submissionRepository = submissionRepository;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
  }

  /**
   * Tạo hội nghị mới
   *
   * @param dto       Thông tin hội nghị cần tạo (name, acronym, description,
   *                  tracks, deadlines)
   * @param creatorId ID của người tạo hội nghị
   * @return ConferenceResponseDTO chứa thông tin hội nghị đã tạo
   */
  @Transactional
  public ConferenceResponseDTO createConference(ConferenceCreateDTO dto, Long creatorId) {
    // 1. Determine actual chairId
    Long tempChairId = creatorId;

    // Check if creator is Admin and specifies a different chair
    User creator = userRepository.findById(creatorId)
        .orElseThrow(() -> new NotFoundException("Creator not found"));

    boolean isAdmin = creator.getRoles().stream()
        .anyMatch(r -> r.getName() == RoleName.ADMIN);

    if (isAdmin && dto.getChairId() != null) {
      tempChairId = dto.getChairId();
      final Long selectedChairId = tempChairId;
      // Ensure the assigned chair exists
      User assignedChair = userRepository.findById(selectedChairId)
          .orElseThrow(() -> new NotFoundException("Assigned chair user not found with ID: " + selectedChairId));

      // Ensure assigned user has ROLE_CHAIR
      boolean hasChairRole = assignedChair.getRoles().stream()
          .anyMatch(r -> r.getName() == RoleName.CHAIR);

      if (!hasChairRole) {
        Role chairRole = roleRepository.findByName(RoleName.CHAIR)
            .orElseGet(() -> {
              Role newRole = Role.builder()
                  .name(RoleName.CHAIR)
                  .description("Role: CHAIR")
                  .build();
              return roleRepository.save(newRole);
            });
        assignedChair.getRoles().add(chairRole);
        userRepository.save(assignedChair);
      }
    }

    final Long chairId = tempChairId;

    Conference.ReviewMode reviewMode = dto.getReviewMode() != null
        ? Conference.ReviewMode.valueOf(dto.getReviewMode())
        : Conference.ReviewMode.DOUBLE_BLIND;

    Conference conference = Conference.builder()
        .name(dto.getName())
        .acronym(dto.getAcronym())
        .description(dto.getDescription())
        .chairId(chairId)
        .published(false)
        .reviewMode(reviewMode)
        .build();

    Conference savedConference = conferenceRepository.save(conference);
    final Conference finalConference = savedConference;

    // Add keywords if provided
    if (dto.getKeywordIds() != null && !dto.getKeywordIds().isEmpty()) {
      List<Keyword> keywords = dto.getKeywordIds().stream()
          .map(
              keywordId -> keywordRepository
                  .findById(keywordId)
                  .orElseThrow(
                      () -> new NotFoundException(
                          "Keyword with id " + keywordId + " not found")))
          .collect(Collectors.toList());
      finalConference.setKeywords(keywords);
    }

    // Add topics if provided
    if (dto.getTopics() != null) {
      List<Topic> topics = dto.getTopics().stream()
          .map(
              topicDTO -> Topic.builder()
                  .conference(finalConference)
                  .name(topicDTO.getName())
                  .description(topicDTO.getDescription())
                  .build())
          .collect(Collectors.toList());
      finalConference.setTopics(topics);
    }

    // Add tracks if provided
    if (dto.getTracks() != null) {
      List<Track> tracks = dto.getTracks().stream()
          .map(
              trackDTO -> Track.builder()
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
      List<Deadline> deadlines = dto.getDeadlines().stream()
          .map(
              deadlineDTO -> Deadline.builder()
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
  @Transactional(readOnly = true)
  public ConferenceResponseDTO getConference(Long id) {
    String errorMessage = "Conference with id " + id + " not found";
    Conference conference = conferenceRepository.findById(id).orElseThrow(() -> new NotFoundException(errorMessage));
    return mapToDTO(conference);
  }

  /**
   * Lấy danh sách các hội nghị đã được public (công khai).
   */
  @Transactional(readOnly = true)
  public List<ConferenceResponseDTO> getPublishedConferences() {
    return conferenceRepository.findByPublishedTrue().stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  /**
   * Lấy danh sách hội nghị mà user đang làm Chair.
   * Nếu user là Admin, trả về tất cả hội nghị.
   */
  @Transactional(readOnly = true)
  public List<ConferenceResponseDTO> getConferencesByChair(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

    boolean isAdmin = user.getRoles().stream()
        .anyMatch(r -> r.getName() == RoleName.ADMIN);

    List<Conference> conferences;
    if (isAdmin) {
      conferences = conferenceRepository.findAll();
    } else {
      conferences = conferenceRepository.findByChairId(userId);
    }

    return conferences.stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  /**
   * Cập nhật thông tin hội nghị.
   * Chỉ có Chair của hội nghị hoặc Admin mới được phép cập nhật.
   */
  @Transactional
  public ConferenceResponseDTO updateConference(Long id, ConferenceUpdateDTO dto, Long userId) {
    String errorMessage = "Conference with id " + id + " not found";
    Conference conference = conferenceRepository.findById(id).orElseThrow(() -> new NotFoundException(errorMessage));

    // Check authorization: Either the chair or an ADMIN
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
    boolean isAdmin = user.getRoles().stream()
        .anyMatch(r -> r.getName() == RoleName.ADMIN);

    if (!isAdmin && !conference.getChairId().equals(userId)) {
      throw new UnauthorizedException("Only conference chair or an admin can update this conference");
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
    if (dto.getReviewMode() != null && !dto.getReviewMode().isBlank()) {
      try {
        conference.setReviewMode(Conference.ReviewMode.valueOf(dto.getReviewMode()));
      } catch (IllegalArgumentException e) {
        throw new BusinessException("Invalid review mode: " + dto.getReviewMode());
      }
    }

    // Smart Update for Topics
    if (dto.getTopics() != null) {
      List<Topic> currentTopics = conference.getTopics();
      List<Topic> topicsToKeep = new ArrayList<>();

      for (TopicDTO topicDTO : dto.getTopics()) {
        if (topicDTO.getId() != null) {
          currentTopics.stream()
              .filter(t -> t.getId().equals(topicDTO.getId()))
              .findFirst()
              .ifPresent(t -> {
                t.setName(topicDTO.getName());
                t.setDescription(topicDTO.getDescription());
                topicsToKeep.add(t);
              });
        } else {
          topicsToKeep.add(Topic.builder()
              .conference(conference)
              .name(topicDTO.getName())
              .description(topicDTO.getDescription())
              .build());
        }
      }
      currentTopics.clear();
      currentTopics.addAll(topicsToKeep);
    }

    // Update keywords if provided
    if (dto.getKeywordIds() != null) {
      List<Keyword> keywords = dto.getKeywordIds().stream()
          .map(
              keywordId -> keywordRepository
                  .findById(keywordId)
                  .orElseThrow(
                      () -> new NotFoundException(
                          "Keyword with id " + keywordId + " not found")))
          .collect(Collectors.toList());
      // Use clear-and-addAll instead of replacement for collections managed by
      // Hibernate
      conference.getKeywords().clear();
      conference.getKeywords().addAll(keywords);
    }

    // Smart Update for Tracks
    if (dto.getTracks() != null) {
      List<Track> currentTracks = conference.getTracks();
      List<Track> tracksToKeep = new ArrayList<>();

      for (TrackDTO trackDTO : dto.getTracks()) {
        if (trackDTO.getId() != null) {
          currentTracks.stream()
              .filter(t -> t.getId().equals(trackDTO.getId()))
              .findFirst()
              .ifPresent(t -> {
                t.setName(trackDTO.getName());
                t.setDescription(trackDTO.getDescription());
                t.setActive(trackDTO.getActive() != null ? trackDTO.getActive() : true);
                tracksToKeep.add(t);
              });
        } else {
          tracksToKeep.add(Track.builder()
              .conference(conference)
              .name(trackDTO.getName())
              .description(trackDTO.getDescription())
              .active(trackDTO.getActive() != null ? trackDTO.getActive() : true)
              .build());
        }
      }
      currentTracks.clear();
      currentTracks.addAll(tracksToKeep);
    }

    // Smart Update for Deadlines
    if (dto.getDeadlines() != null) {
      List<Deadline> currentDeadlines = conference.getDeadlines();
      List<Deadline> deadlinesToKeep = new ArrayList<>();

      for (DeadlineDTO deadlineDTO : dto.getDeadlines()) {
        try {
          Deadline.DeadlineType type = Deadline.DeadlineType.valueOf(deadlineDTO.getType());
          if (deadlineDTO.getId() != null) {
            currentDeadlines.stream()
                .filter(d -> d.getId().equals(deadlineDTO.getId()))
                .findFirst()
                .ifPresent(d -> {
                  d.setType(type);
                  d.setDueDate(deadlineDTO.getDueDate());
                  d.setDescription(deadlineDTO.getDescription());
                  d.setHardDeadline(deadlineDTO.getHardDeadline() != null ? deadlineDTO.getHardDeadline() : true);
                  deadlinesToKeep.add(d);
                });
          } else {
            deadlinesToKeep.add(Deadline.builder()
                .conference(conference)
                .type(type)
                .dueDate(deadlineDTO.getDueDate())
                .description(deadlineDTO.getDescription())
                .hardDeadline(deadlineDTO.getHardDeadline() != null ? deadlineDTO.getHardDeadline() : true)
                .build());
          }
        } catch (IllegalArgumentException | NullPointerException e) {
          throw new BusinessException("Invalid deadline type: " + deadlineDTO.getType());
        }
      }
      currentDeadlines.clear();
      currentDeadlines.addAll(deadlinesToKeep);
    }

    Conference updatedConference = conferenceRepository.save(conference);
    return mapToDTO(updatedConference);
  }

  /**
   * Xóa hội nghị.
   * Chỉ xóa được nếu hội nghị chưa có bài nộp nào.
   */
  @Transactional
  public void deleteConference(Long id, Long chairId) {
    String errorMessage = "Conference with id " + id + " not found";
    Conference conference = conferenceRepository.findById(id).orElseThrow(() -> new NotFoundException(errorMessage));

    // Check authorization: Either the chair or an ADMIN
    User user = userRepository.findById(chairId)
        .orElseThrow(() -> new NotFoundException("User not found with ID: " + chairId));
    boolean isAdmin = user.getRoles().stream()
        .anyMatch(r -> r.getName() == RoleName.ADMIN);

    if (!isAdmin && !conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair or an admin can delete this conference");
    }

    // Check if conference has submissions
    long submissionCount = submissionRepository.findByConferenceId(id).size();
    if (submissionCount > 0) {
      throw new BusinessException(
          "Cannot delete conference. Conference has "
              + submissionCount
              + " submission(s). Please delete or reassign submissions first.");
    }

    conferenceRepository.delete(conference);
  }

  private ConferenceResponseDTO mapToDTO(Conference conference) {
    List<TopicDTO> topics = conference.getTopics().stream()
        .map(
            topic -> TopicDTO.builder()
                .id(topic.getId())
                .name(topic.getName())
                .description(topic.getDescription())
                .build())
        .collect(Collectors.toList());

    List<KeywordDTO> keywords = conference.getKeywords().stream()
        .map(
            keyword -> KeywordDTO.builder()
                .id(keyword.getId())
                .name(keyword.getName())
                .description(keyword.getDescription())
                .build())
        .collect(Collectors.toList());

    List<TrackDTO> tracks = conference.getTracks().stream()
        .map(
            track -> TrackDTO.builder()
                .id(track.getId())
                .name(track.getName())
                .description(track.getDescription())
                .active(track.getActive())
                .build())
        .collect(Collectors.toList());

    List<DeadlineDTO> deadlines = conference.getDeadlines().stream()
        .map(
            deadline -> DeadlineDTO.builder()
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
        .reviewMode(conference.getReviewMode().name())
        .topics(topics)
        .keywords(keywords)
        .tracks(tracks)
        .deadlines(deadlines)
        .cfp(mapCFPToDTO(conference.getCfp()))
        .createdAt(conference.getCreatedAt())
        .updatedAt(conference.getUpdatedAt())
        .build();
  }

  private CFPResponseDTO mapCFPToDTO(CFP cfp) {
    if (cfp == null) {
      return null;
    }

    List<TopicDTO> topicsList = cfp.getConference().getTopics().stream()
        .map(
            topic -> TopicDTO.builder()
                .id(topic.getId())
                .name(topic.getName())
                .description(topic.getDescription())
                .build())
        .collect(Collectors.toList());

    return CFPResponseDTO.builder()
        .id(cfp.getId())
        .callForPapers(cfp.getCallForPapers())
        .topicsList(topicsList)
        .submissionGuidelines(cfp.getSubmissionGuidelines())
        .open(cfp.getOpen())
        .createdAt(cfp.getCreatedAt())
        .updatedAt(cfp.getUpdatedAt())
        .build();
  }
}
