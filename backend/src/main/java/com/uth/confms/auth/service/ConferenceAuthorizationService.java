package com.uth.confms.auth.service;

import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.pc.entity.PCMember;
import com.uth.confms.pc.repository.PCMemberRepository;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.exception.NotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service để check conference-level authorization
 *
 * <p>Service này cung cấp các methods để check:
 * <ul>
 *   <li>User có phải là chair của conference không
 *   <li>User có phải là PC member của conference không
 *   <li>User có phải là PC member với status ACCEPTED không
 * </ul>
 *
 * <p>Các methods này có thể được sử dụng trong:
 * <ul>
 *   <li>@PreAuthorize SpEL expressions
 *   <li>Business logic checks
 *   <li>Service layer authorization
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
public class ConferenceAuthorizationService {

  private final ConferenceRepository conferenceRepository;
  private final PCMemberRepository pcMemberRepository;
  private final UserRepository userRepository;

  public ConferenceAuthorizationService(
      ConferenceRepository conferenceRepository,
      PCMemberRepository pcMemberRepository,
      UserRepository userRepository) {
    this.conferenceRepository = conferenceRepository;
    this.pcMemberRepository = pcMemberRepository;
    this.userRepository = userRepository;
  }

  /**
   * Check nếu user là chair của conference
   *
   * @param userId User ID
   * @param conferenceId Conference ID
   * @return true nếu user là chair của conference
   */
  public boolean isChairOfConference(Long userId, Long conferenceId) {
    Conference conference = conferenceRepository
        .findById(conferenceId)
        .orElseThrow(() -> new NotFoundException("Conference not found: " + conferenceId));
    return conference.getChairId().equals(userId);
  }

  /**
   * Check nếu user là chair của conference (by email)
   *
   * @param email User email
   * @param conferenceId Conference ID
   * @return true nếu user là chair của conference
   */
  public boolean isChairOfConferenceByEmail(String email, Long conferenceId) {
    Long userId = userRepository
        .findByEmail(email)
        .map(user -> user.getId())
        .orElseThrow(() -> new NotFoundException("User not found: " + email));
    @SuppressWarnings("null")
    Long nonNullUserId = userId;
    return isChairOfConference(nonNullUserId, conferenceId);
  }

  /**
   * Check nếu user là PC member của conference
   *
   * @param userId User ID
   * @param conferenceId Conference ID
   * @return true nếu user là PC member của conference (bất kỳ status nào)
   */
  public boolean isPCMemberOfConference(Long userId, Long conferenceId) {
    return pcMemberRepository
        .findByConferenceIdAndUserId(conferenceId, userId)
        .isPresent();
  }

  /**
   * Check nếu user là PC member của conference với status ACCEPTED
   *
   * @param userId User ID
   * @param conferenceId Conference ID
   * @return true nếu user là PC member với status ACCEPTED
   */
  public boolean isAcceptedPCMemberOfConference(Long userId, Long conferenceId) {
    return pcMemberRepository
        .findByConferenceIdAndUserId(conferenceId, userId)
        .map(member -> member.getStatus() == PCMember.PCMemberStatus.ACCEPTED)
        .orElse(false);
  }

  /**
   * Check nếu user là PC member của conference (by email)
   *
   * @param email User email
   * @param conferenceId Conference ID
   * @return true nếu user là PC member của conference
   */
  public boolean isPCMemberOfConferenceByEmail(String email, Long conferenceId) {
    Long userId = userRepository
        .findByEmail(email)
        .map(user -> user.getId())
        .orElseThrow(() -> new NotFoundException("User not found: " + email));
    @SuppressWarnings("null")
    Long nonNullUserId = userId;
    return isPCMemberOfConference(nonNullUserId, conferenceId);
  }

  /**
   * Check nếu user là PC member với status ACCEPTED (by email)
   *
   * @param email User email
   * @param conferenceId Conference ID
   * @return true nếu user là PC member với status ACCEPTED
   */
  public boolean isAcceptedPCMemberOfConferenceByEmail(String email, Long conferenceId) {
    Long userId = userRepository
        .findByEmail(email)
        .map(user -> user.getId())
        .orElseThrow(() -> new NotFoundException("User not found: " + email));
    @SuppressWarnings("null")
    Long nonNullUserId = userId;
    return isAcceptedPCMemberOfConference(nonNullUserId, conferenceId);
  }

  /**
   * Check nếu user có quyền truy cập conference (chair hoặc PC member)
   *
   * @param userId User ID
   * @param conferenceId Conference ID
   * @return true nếu user là chair hoặc PC member
   */
  public boolean hasConferenceAccess(Long userId, Long conferenceId) {
    return isChairOfConference(userId, conferenceId)
        || isPCMemberOfConference(userId, conferenceId);
  }

  /**
   * Check nếu user có quyền truy cập conference (by email)
   *
   * @param email User email
   * @param conferenceId Conference ID
   * @return true nếu user là chair hoặc PC member
   */
  public boolean hasConferenceAccessByEmail(String email, Long conferenceId) {
    Long userId = userRepository
        .findByEmail(email)
        .map(user -> user.getId())
        .orElseThrow(() -> new NotFoundException("User not found: " + email));
    @SuppressWarnings("null")
    Long nonNullUserId = userId;
    return hasConferenceAccess(nonNullUserId, conferenceId);
  }
}
