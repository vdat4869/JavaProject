package com.uth.confms.conference.service;

import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.conference.dto.CFPDTO;
import com.uth.confms.conference.dto.CFPResponseDTO;
import com.uth.confms.conference.entity.CFP;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.repository.CFPRepository;
import com.uth.confms.conference.repository.ConferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class CFPService {
  private final CFPRepository cfpRepository;
  private final ConferenceRepository conferenceRepository;

  public CFPService(CFPRepository cfpRepository, ConferenceRepository conferenceRepository) {
    this.cfpRepository = cfpRepository;
    this.conferenceRepository = conferenceRepository;
  }

  public CFPResponseDTO getCFPByConference(Long conferenceId) {
    String conferenceErrorMessage = "Conference with id " + conferenceId + " not found";
    Conference conference =
        conferenceRepository
            .findById(conferenceId)
            .orElseThrow(() -> new NotFoundException(conferenceErrorMessage));

    CFP cfp =
        cfpRepository
            .findByConference(conference)
            .orElseThrow(() -> new NotFoundException("CFP not found for this conference"));

    return mapToDTO(cfp);
  }

  @Transactional
  public CFPResponseDTO createOrUpdateCFP(CFPDTO dto, Long chairId) {
    String conferenceErrorMessage = "Conference with id " + dto.getConferenceId() + " not found";
    Conference conference =
        conferenceRepository
            .findById(dto.getConferenceId())
            .orElseThrow(() -> new NotFoundException(conferenceErrorMessage));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can manage CFP");
    }

    CFP cfp = cfpRepository.findByConference(conference).orElse(null);

    if (cfp == null) {
      // Create new CFP
      cfp =
          CFP.builder()
              .conference(conference)
              .callForPapers(dto.getCallForPapers())
              .topics(dto.getTopics())
              .submissionGuidelines(dto.getSubmissionGuidelines())
              .open(dto.getOpen() != null ? dto.getOpen() : false)
              .build();
    } else {
      // Update existing CFP
      if (dto.getCallForPapers() != null) {
        cfp.setCallForPapers(dto.getCallForPapers());
      }
      if (dto.getTopics() != null) {
        cfp.setTopics(dto.getTopics());
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

  @Transactional
  public CFPResponseDTO publishCFP(Long conferenceId, Long chairId) {
    String conferenceErrorMessage = "Conference with id " + conferenceId + " not found";
    Conference conference =
        conferenceRepository
            .findById(conferenceId)
            .orElseThrow(() -> new NotFoundException(conferenceErrorMessage));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can publish CFP");
    }

    CFP cfp =
        cfpRepository
            .findByConference(conference)
            .orElseThrow(() -> new NotFoundException("CFP not found for this conference"));

    cfp.setOpen(true);
    CFP savedCfp = cfpRepository.save(cfp);

    return mapToDTO(savedCfp);
  }

  @Transactional
  public CFPResponseDTO closeCFP(Long conferenceId, Long chairId) {
    String conferenceErrorMessage = "Conference with id " + conferenceId + " not found";
    Conference conference =
        conferenceRepository
            .findById(conferenceId)
            .orElseThrow(() -> new NotFoundException(conferenceErrorMessage));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can close CFP");
    }

    CFP cfp =
        cfpRepository
            .findByConference(conference)
            .orElseThrow(() -> new NotFoundException("CFP not found for this conference"));

    cfp.setOpen(false);
    CFP savedCfp = cfpRepository.save(cfp);

    return mapToDTO(savedCfp);
  }

  private CFPResponseDTO mapToDTO(CFP cfp) {
    return CFPResponseDTO.builder()
        .id(cfp.getId())
        .callForPapers(cfp.getCallForPapers())
        .topics(cfp.getTopics())
        .submissionGuidelines(cfp.getSubmissionGuidelines())
        .open(cfp.getOpen())
        .createdAt(cfp.getCreatedAt())
        .updatedAt(cfp.getUpdatedAt())
        .build();
  }
}
