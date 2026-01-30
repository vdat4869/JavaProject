package com.uth.confms.pc.service;

import com.uth.confms.auth.entity.Role;
import com.uth.confms.auth.enums.RoleName;
import com.uth.confms.auth.repository.RoleRepository;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.pc.dto.PCInvitationResponseDTO;
import com.uth.confms.pc.dto.PCInviteDTO;
import com.uth.confms.pc.dto.PCMemberDTO;
import com.uth.confms.pc.entity.PCInvitation;
import com.uth.confms.pc.entity.PCMember;
import com.uth.confms.pc.repository.PCInvitationRepository;
import com.uth.confms.pc.repository.PCMemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý Program Committee (PC) members và invitations
 *
 * <p>
 * Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 * <li>Mời PC members qua email
 * <li>Accept/decline PC invitations
 * <li>Quản lý danh sách PC members
 * <li>Gửi invitation emails với token
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class PCService {
  private final PCMemberRepository pcMemberRepository;
  private final PCInvitationRepository pcInvitationRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final ConferenceRepository conferenceRepository;
  private final JavaMailSender mailSender;

  @Value("${app.pc.invitation.expiration-days:7}")
  private int invitationExpirationDays;

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  public PCService(
      PCMemberRepository pcMemberRepository,
      PCInvitationRepository pcInvitationRepository,
      UserRepository userRepository,
      RoleRepository roleRepository,
      ConferenceRepository conferenceRepository,
      JavaMailSender mailSender) {
    this.pcMemberRepository = pcMemberRepository;
    this.pcInvitationRepository = pcInvitationRepository;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.conferenceRepository = conferenceRepository;
    this.mailSender = mailSender;
  }

  /**
   * Mời PC member tham gia hội nghị
   *
   * @param dto     Thông tin invitation (conferenceId, email)
   * @param chairId ID của chair gửi invitation
   * @return PCInvitationResponseDTO chứa thông tin invitation đã tạo
   * @throws NotFoundException     Nếu không tìm thấy conference hoặc user
   * @throws UnauthorizedException Nếu không phải chair của conference
   * @throws BusinessException     Nếu user đã là PC member hoặc đã có invitation
   */
  @Transactional
  // Mời thành viên tham gia PC
  public PCInvitationResponseDTO invitePCMember(PCInviteDTO dto, Long chairId) {
    Conference conference = conferenceRepository
        .findById(dto.getConferenceId())
        .orElseThrow(
            () -> new NotFoundException(
                "Conference with id " + dto.getConferenceId() + " not found"));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can invite PC members");
    }

    // Find user by email
    User user = userRepository
        .findByEmail(dto.getEmail())
        .orElseThrow(
            () -> new NotFoundException("User with email " + dto.getEmail() + " not found"));

    // Check if already a PC member
    if (pcMemberRepository
        .findByConferenceIdAndUserId(dto.getConferenceId(), user.getId())
        .isPresent()) {
      throw new BusinessException("User is already a PC member for this conference");
    }

    // Check if invitation already exists
    PCInvitation existingInvitation = pcInvitationRepository
        .findByConferenceIdAndInvitedUserId(dto.getConferenceId(), user.getId())
        .orElse(null);

    String token = UUID.randomUUID().toString();
    PCInvitation invitation;

    if (existingInvitation != null) {
      // If invitation exists but is PENDING or ACCEPTED, block new invitation
      if (existingInvitation.getStatus() == PCInvitation.InvitationStatus.PENDING
          || existingInvitation.getStatus() == PCInvitation.InvitationStatus.ACCEPTED) {
        throw new BusinessException("Invitation already sent to this user or user already accepted");
      }

      // If DECLINED or EXPIRED, reuse existing record and update status
      existingInvitation.setToken(token);
      existingInvitation.setStatus(PCInvitation.InvitationStatus.PENDING);
      existingInvitation.setExpiresAt(LocalDateTime.now().plusDays(invitationExpirationDays));
      existingInvitation.setInvitedBy(chairId); // Update inviter if needed
      invitation = pcInvitationRepository.save(existingInvitation);
    } else {
      // Create new invitation
      invitation = PCInvitation.builder()
          .conferenceId(dto.getConferenceId())
          .invitedUserId(user.getId())
          .invitedBy(chairId)
          .token(token)
          .status(PCInvitation.InvitationStatus.PENDING)
          .expiresAt(LocalDateTime.now().plusDays(invitationExpirationDays))
          .build();
      invitation = pcInvitationRepository.save(invitation);
    }

    // Send invitation email
    sendInvitationEmail(user.getEmail(), conference.getName(), token);

    return mapInvitationToDTO(invitation, user);
  }

  @Transactional
  // Chấp nhận lời mời tham gia PC
  public PCMemberDTO acceptInvitation(String token, Long userId) {
    PCInvitation invitation = pcInvitationRepository
        .findByToken(token)
        .orElseThrow(() -> new NotFoundException("Invalid invitation token"));

    // Check if invitation is for this user
    if (!invitation.getInvitedUserId().equals(userId)) {
      throw new UnauthorizedException("This invitation is not for you");
    }

    // Check if invitation is expired
    if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
      invitation.setStatus(PCInvitation.InvitationStatus.EXPIRED);
      pcInvitationRepository.save(invitation);
      throw new BusinessException("Invitation has expired");
    }

    // Check if already accepted
    if (invitation.getStatus() == PCInvitation.InvitationStatus.ACCEPTED) {
      throw new BusinessException("Invitation already accepted");
    }

    // Create PC member
    PCMember pcMember = PCMember.builder()
        .conferenceId(invitation.getConferenceId())
        .userId(invitation.getInvitedUserId())
        .status(PCMember.PCMemberStatus.ACCEPTED)
        .build();

    pcMember = pcMemberRepository.save(pcMember);

    // Update invitation status
    invitation.setStatus(PCInvitation.InvitationStatus.ACCEPTED);
    pcInvitationRepository.save(invitation);

    User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    // Assign roles to user
    Role pcRole = roleRepository.findByName(RoleName.PC)
        .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.PC).description("Role: PC").build()));

    user.getRoles().add(pcRole);
    userRepository.save(user);

    return mapMemberToDTO(pcMember, user);
  }

  @Transactional
  // Từ chối lời mời
  public void declineInvitation(String token, Long userId) {
    PCInvitation invitation = pcInvitationRepository
        .findByToken(token)
        .orElseThrow(() -> new NotFoundException("Invalid invitation token"));

    // Check if invitation is for this user
    if (!invitation.getInvitedUserId().equals(userId)) {
      throw new UnauthorizedException("This invitation is not for you");
    }

    // Update invitation status
    invitation.setStatus(PCInvitation.InvitationStatus.DECLINED);
    pcInvitationRepository.save(invitation);
  }

  public PCMemberDTO getMembership(Long conferenceId, Long userId) {
    return pcMemberRepository.findByConferenceIdAndUserId(conferenceId, userId)
        .map(member -> {
          User user = userRepository.findById(userId).orElse(null);
          return mapMemberToDTO(member, user);
        })
        .orElse(null);
  }

  // Lấy danh sách PC members
  public List<PCMemberDTO> getPCMembers(Long conferenceId, Long chairId) {
    Conference conference = conferenceRepository
        .findById(conferenceId)
        .orElseThrow(
            () -> new NotFoundException("Conference with id " + conferenceId + " not found"));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can view PC members");
    }

    return pcMemberRepository.findByConferenceId(conferenceId).stream()
        .map(
            member -> {
              User user = userRepository.findById(member.getUserId()).orElse(null);
              return mapMemberToDTO(member, user);
            })
        .collect(Collectors.toList());
  }

  public List<PCInvitationResponseDTO> getInvitations(Long conferenceId, Long chairId) {
    Conference conference = conferenceRepository
        .findById(conferenceId)
        .orElseThrow(
            () -> new NotFoundException("Conference with id " + conferenceId + " not found"));

    // Check authorization
    if (!conference.getChairId().equals(chairId)) {
      throw new UnauthorizedException("Only conference chair can view invitations");
    }

    return pcInvitationRepository.findByConferenceId(conferenceId).stream()
        .map(
            invitation -> {
              User user = userRepository.findById(invitation.getInvitedUserId()).orElse(null);
              return mapInvitationToDTO(invitation, user);
            })
        .collect(Collectors.toList());
  }

  // Gửi email mời với token
  private void sendInvitationEmail(String email, String conferenceName, String token) {
    try {
      jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
      org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(
          message, false, "UTF-8");

      helper.setTo(email);
      helper.setSubject("Invitation to PC Member - " + conferenceName);
      helper.setText(
          String.format(
              "Xin chào,\n\n"
                  + "Bạn đã được mời làm thành viên Program Committee (PC) cho hội nghị:\n"
                  + "=== %s ===\n\n"
                  + "Vui lòng xem chi tiết lời mời và phản hồi tại đường dẫn sau:\n\n"
                  + "👉 Xem thư mời:\n"
                  + "%s/app/pc/invitation?token=%s\n\n"
                  + "Lưu ý: Link này có hiệu lực trong %d ngày.\n"
                  + "Sau khi chấp nhận, bạn sẽ được chuyển đến trang khai báo mâu thuẫn lợi ích (COI).\n\n"
                  + "Trân trọng,\n"
                  + "UTH-ConfMS Team",
              conferenceName, frontendUrl, token, invitationExpirationDays),
          false);
      mailSender.send(message);
    } catch (Exception e) {
      System.err.println("Failed to send invitation email: " + e.getMessage());
    }
  }

  private PCMemberDTO mapMemberToDTO(PCMember member, User user) {
    return PCMemberDTO.builder()
        .id(member.getId())
        .conferenceId(member.getConferenceId())
        .userId(member.getUserId())
        .email(user != null ? user.getEmail() : null)
        .fullName(user != null ? user.getFullName() : null)
        .status(member.getStatus().name())
        .createdAt(member.getCreatedAt())
        .updatedAt(member.getUpdatedAt())
        .build();
  }

  private PCInvitationResponseDTO mapInvitationToDTO(PCInvitation invitation, User user) {
    return PCInvitationResponseDTO.builder()
        .id(invitation.getId())
        .conferenceId(invitation.getConferenceId())
        .invitedUserEmail(user != null ? user.getEmail() : null)
        .status(invitation.getStatus().name())
        .expiresAt(invitation.getExpiresAt())
        .createdAt(invitation.getCreatedAt())
        .build();
  }
}
