package com.uth.confms.review.repository;

import com.uth.confms.review.entity.ReviewTemplate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewTemplateRepository extends JpaRepository<ReviewTemplate, Long> {
  // Tìm template của conference
  List<ReviewTemplate> findByConferenceId(Long conferenceId);

  // Tìm global templates
  List<ReviewTemplate> findByConferenceIdIsNull(); // Global templates

  // Tìm default template của conference
  Optional<ReviewTemplate> findByConferenceIdAndIsDefaultTrue(Long conferenceId);

  // Tìm global default template
  Optional<ReviewTemplate> findByConferenceIdIsNullAndIsDefaultTrue(); // Global default template
}
