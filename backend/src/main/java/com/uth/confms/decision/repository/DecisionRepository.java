package com.uth.confms.decision.repository;

import com.uth.confms.decision.entity.Decision;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DecisionRepository extends JpaRepository<Decision, Long> {
  Optional<Decision> findBySubmissionId(Long submissionId); // Tìm decision của submission

  List<Decision> findBySubmissionIdIn(List<Long> submissionIds); // Tìm decision cho list submissions

  List<Decision> findByDecidedBy(Long decidedBy); // Tìm decision bởi Chair

  List<Decision> findByType(Decision.DecisionType type); // Tìm decision theo loại

  List<Decision> findByNotifiedFalse(); // Tìm decision chưa notify
}
