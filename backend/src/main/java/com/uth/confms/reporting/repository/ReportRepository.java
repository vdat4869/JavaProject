package com.uth.confms.reporting.repository;

import com.uth.confms.reporting.entity.ReportSnapshot;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<ReportSnapshot, Long> {
  List<ReportSnapshot> findByConferenceId(Long conferenceId);

  Optional<ReportSnapshot> findFirstByConferenceIdOrderBySnapshotAtDesc(Long conferenceId);

  List<ReportSnapshot> findByConferenceIdOrderBySnapshotAtDesc(Long conferenceId);
}
