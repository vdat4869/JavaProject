package com.uth.confms.decision.repository;

import com.uth.confms.decision.entity.Decision;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DecisionRepository extends JpaRepository<Decision, Long> {
  Optional<Decision> findBySubmissionId(Long submissionId);

  List<Decision> findByDecidedBy(Long decidedBy);

  List<Decision> findByType(Decision.DecisionType type);

  List<Decision> findByNotifiedFalse();
}
