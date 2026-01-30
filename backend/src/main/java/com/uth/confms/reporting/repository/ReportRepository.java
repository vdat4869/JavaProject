package com.uth.confms.reporting.repository;

import com.uth.confms.reporting.entity.ReportSnapshot;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<ReportSnapshot, Long> {
  // Tìm snapshots theo conference
  List<ReportSnapshot> findByConferenceId(Long conferenceId);

  // Lấy snapshot mới nhất
  Optional<ReportSnapshot> findFirstByConferenceIdOrderBySnapshotAtDesc(Long conferenceId);

  // Lấy lịch sử snapshots giảm dần theo thời gian
  List<ReportSnapshot> findByConferenceIdOrderBySnapshotAtDesc(Long conferenceId);
}
