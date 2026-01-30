package com.uth.confms.conference.repository;

import com.uth.confms.conference.entity.Track;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
  List<Track> findByConferenceId(Long conferenceId); // Tìm tracks theo hội nghị
}
