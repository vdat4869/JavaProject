package com.uth.confms.decision.repository;

import com.uth.confms.decision.entity.DecisionHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DecisionHistoryRepository extends JpaRepository<DecisionHistory, Long> {
  List<DecisionHistory> findByDecisionIdOrderByChangedAtDesc(Long decisionId);

  List<DecisionHistory> findByDecisionIdAndChangeTypeOrderByChangedAtDesc(
      Long decisionId, DecisionHistory.ChangeType changeType);
}
