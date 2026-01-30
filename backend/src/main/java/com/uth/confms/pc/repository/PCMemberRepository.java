package com.uth.confms.pc.repository;

import com.uth.confms.pc.entity.PCMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PCMemberRepository extends JpaRepository<PCMember, Long> {
  // Tìm tất cả thành viên PC của conference
  List<PCMember> findByConferenceId(Long conferenceId);

  // Tìm các conference mà user tham gia làm PC
  List<PCMember> findByUserId(Long userId);

  // Tìm thành viên PC cụ thể
  Optional<PCMember> findByConferenceIdAndUserId(Long conferenceId, Long userId);

  List<PCMember> findByConferenceIdAndStatus(Long conferenceId, PCMember.PCMemberStatus status);

  @EntityGraph(attributePaths = { "expertiseTopics" })
  @Query("SELECT p FROM PCMember p WHERE p.conferenceId = :conferenceId AND p.status = :status")
  List<PCMember> findByConferenceIdAndStatusWithExpertise(
      @Param("conferenceId") Long conferenceId, @Param("status") PCMember.PCMemberStatus status);
}
