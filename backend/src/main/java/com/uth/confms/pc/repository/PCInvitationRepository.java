package com.uth.confms.pc.repository;

import com.uth.confms.pc.entity.PCInvitation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PCInvitationRepository extends JpaRepository<PCInvitation, Long> {
  // Tìm lời mời theo token
  Optional<PCInvitation> findByToken(String token);

  // Tìm danh sách lời mời của conference
  List<PCInvitation> findByConferenceId(Long conferenceId);

  // Tìm lời mời gửi đến user cụ thể
  List<PCInvitation> findByInvitedUserId(Long userId);

  Optional<PCInvitation> findByConferenceIdAndInvitedUserId(Long conferenceId, Long userId);
}
