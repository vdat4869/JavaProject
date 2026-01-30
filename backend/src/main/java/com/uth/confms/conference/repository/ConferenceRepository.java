package com.uth.confms.conference.repository;

import com.uth.confms.conference.entity.Conference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConferenceRepository extends JpaRepository<Conference, Long> {
  List<Conference> findByPublishedTrue(); // Tìm các hội nghị đã public

  List<Conference> findByChairId(Long chairId); // Tìm các hội nghị theo Chair ID
}
