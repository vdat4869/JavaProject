package com.uth.confms.conference.repository;

import com.uth.confms.conference.entity.CFP;
import com.uth.confms.conference.entity.Conference;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CFPRepository extends JpaRepository<CFP, Long> {
  Optional<CFP> findByConference(Conference conference); // Tìm CFP theo hội nghị
}
