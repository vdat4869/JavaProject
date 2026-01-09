package com.uth.confms.pc.repository;

import com.uth.confms.pc.entity.PCMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PCMemberRepository extends JpaRepository<PCMember, Long> {
  List<PCMember> findByConferenceId(Long conferenceId);

  List<PCMember> findByUserId(Long userId);

  Optional<PCMember> findByConferenceIdAndUserId(Long conferenceId, Long userId);

  List<PCMember> findByConferenceIdAndStatus(Long conferenceId, PCMember.PCMemberStatus status);
}
