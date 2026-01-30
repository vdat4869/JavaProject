package com.uth.confms.conference.repository;

import com.uth.confms.conference.entity.Topic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
  List<Topic> findByConferenceId(Long conferenceId); // Tìm topics theo hội nghị
}
