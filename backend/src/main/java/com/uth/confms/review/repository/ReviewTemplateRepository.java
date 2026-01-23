package com.uth.confms.review.repository;

import com.uth.confms.review.entity.ReviewTemplate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewTemplateRepository extends JpaRepository<ReviewTemplate, Long> {
  List<ReviewTemplate> findByConferenceId(Long conferenceId);

  List<ReviewTemplate> findByConferenceIdIsNull(); // Global templates

  Optional<ReviewTemplate> findByConferenceIdAndIsDefaultTrue(Long conferenceId);

  Optional<ReviewTemplate> findByConferenceIdIsNullAndIsDefaultTrue(); // Global default template
}
