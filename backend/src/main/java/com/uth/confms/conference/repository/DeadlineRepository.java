package com.uth.confms.conference.repository;

import com.uth.confms.conference.entity.Deadline;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeadlineRepository extends JpaRepository<Deadline, Long> {
  List<Deadline> findByConferenceId(Long conferenceId); // Tìm deadlines theo hội nghị

  List<Deadline> findByConferenceIdIn(List<Long> conferenceIds); // Tìm deadlines cho nhiều hội nghị
}
