package com.uth.confms.conference.service;

import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.conference.dto.CFPDTO;
import com.uth.confms.conference.dto.CFPResponseDTO;
import com.uth.confms.conference.dto.TopicDTO;
import com.uth.confms.conference.dto.TrackDTO;
import com.uth.confms.conference.entity.CFP;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.entity.Topic;
import com.uth.confms.conference.repository.CFPRepository;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.conference.repository.TopicRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class CFPService {
  private final CFPRepository cfpRepository;
  private final ConferenceRepository conferenceRepository;
  @SuppressWarnings("unused")
  private final TopicRepository topicRepository; // Reserved for future use

  public CFPService(
      CFPRepository cfpRepository,
      ConferenceRepository conferenceRepository,
      TopicRepository topicRepository) {
    this.cfpRepository = cfpRepository;
    this.conferenceRepository = conferenceRepository;
    this.topicRepository = topicRepository;
  }

  /**
   * Lấy thông tin CFP của một hội nghị.
   */
  @Transactional(readOnly = true)
  public CFPResponseDTO getCFPByConference(Long conferenceId) {
    String conferenceErrorMessage = "Conference with id " + conferenceId + " not found";
    Conference conference = conferenceRepository
        .findById(conferenceId)
        .orElseThrow(() -> new NotFoundException(conferenceErrorMessage));

    CFP cfp = cfpRepository
        .findByConference(conference)
        .orElseThrow(() -> new NotFoundException("CFP not found for this conference"));

    return mapToDTO(cfp);
  }

  /**
   * Tạo hoặc cập nhật thông tin CFP.
   * Chỉ Chair của hội nghị mới được phép thực hiện.
   */
  @Transactional
  @SuppressWarnings("deprecation")
  public CFPResponseDTO createOrUpdateCFP(CFPDTO dto, Long chairId) {
    String conferenceErrorMessage = "Conference with id " + dto.getConferenceId() + " not found";
    Conference conference = conferenceRepository
        .findById(dto.getConferenceId())
        .orElseThrow(() -> new NotFoundException(conferenceErrorMessage));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can manage CFP");
    }

    CFP cfp = cfpRepository.findByConference(conference).orElse(null);

    // Handle topicIds: Link CFP to Conference topics
    if (dto.getTopicIds() != null && !dto.getTopicIds().isEmpty()) {
      // Validate that all topicIds belong to this conference
      List<Topic> conferenceTopics = conference.getTopics();
      List<Long> validTopicIds = conferenceTopics.stream().map(Topic::getId).collect(Collectors.toList());
      for (Long topicId : dto.getTopicIds()) {
        if (!validTopicIds.contains(topicId)) {
          throw new com.uth.confms.common.exception.BusinessException(
              "Topic with id " + topicId + " does not belong to this conference");
        }
      }
    }

    if (cfp == null) {
      // Create new CFP
      cfp = CFP.builder()
          .conference(conference)
          .callForPapers(dto.getCallForPapers())
          .topics(dto.getTopics()) // Keep for backward compatibility
          .submissionGuidelines(dto.getSubmissionGuidelines())
          .open(dto.getOpen() != null ? dto.getOpen() : false)
          .build();
    } else {
      // Update existing CFP
      if (dto.getCallForPapers() != null) {
        cfp.setCallForPapers(dto.getCallForPapers());
      }
      if (dto.getTopics() != null) {
        cfp.setTopics(dto.getTopics()); // Keep for backward compatibility
      }
      if (dto.getSubmissionGuidelines() != null) {
        cfp.setSubmissionGuidelines(dto.getSubmissionGuidelines());
      }
      if (dto.getOpen() != null) {
        cfp.setOpen(dto.getOpen());
      }
    }

    cfp = cfpRepository.save(cfp);
    return mapToDTO(cfp);
  }

  /**
   * Mở CFP (cho phép nộp bài).
   */
  @Transactional
  public CFPResponseDTO publishCFP(Long conferenceId, Long chairId) {
    String conferenceErrorMessage = "Conference with id " + conferenceId + " not found";
    Conference conference = conferenceRepository
        .findById(conferenceId)
        .orElseThrow(() -> new NotFoundException(conferenceErrorMessage));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can publish CFP");
    }

    CFP cfp = cfpRepository
        .findByConference(conference)
        .orElseThrow(() -> new NotFoundException("CFP not found for this conference"));

    cfp.setOpen(true);
    CFP savedCfp = cfpRepository.save(cfp);

    return mapToDTO(savedCfp);
  }

  /**
   * Đóng CFP (ngưng nhận bài).
   */
  @Transactional
  public CFPResponseDTO closeCFP(Long conferenceId, Long chairId) {
    String conferenceErrorMessage = "Conference with id " + conferenceId + " not found";
    Conference conference = conferenceRepository
        .findById(conferenceId)
        .orElseThrow(() -> new NotFoundException(conferenceErrorMessage));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can close CFP");
    }

    CFP cfp = cfpRepository
        .findByConference(conference)
        .orElseThrow(() -> new NotFoundException("CFP not found for this conference"));

    cfp.setOpen(false);
    CFP savedCfp = cfpRepository.save(cfp);

    return mapToDTO(savedCfp);
  }

  @SuppressWarnings("deprecation")
  private CFPResponseDTO mapToDTO(CFP cfp) {
    // Get structured topics from Conference
    Conference conference = cfp.getConference();
    List<TopicDTO> topicsList = conference.getTopics().stream()
        .map(
            topic -> TopicDTO.builder()
                .id(topic.getId())
                .name(topic.getName())
                .description(topic.getDescription())
                .build())
        .collect(Collectors.toList());

    List<TrackDTO> tracks = conference.getTracks().stream()
        .map(track -> TrackDTO.builder()
            .id(track.getId())
            .name(track.getName())
            .description(track.getDescription())
            .active(track.getActive())
            .build())
        .collect(Collectors.toList());

    return CFPResponseDTO.builder()
        .id(cfp.getId())
        .callForPapers(cfp.getCallForPapers())
        .topics(cfp.getTopics()) // Keep for backward compatibility
        .topicsList(topicsList) // Structured topics from Conference
        .tracks(tracks) // Tracks from Conference
        .submissionGuidelines(cfp.getSubmissionGuidelines())
        .open(cfp.getOpen())
        .createdAt(cfp.getCreatedAt())
        .updatedAt(cfp.getUpdatedAt())
        .build();
  }
}
