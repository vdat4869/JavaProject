package com.uth.confms.pc.repository;

import com.uth.confms.pc.entity.ConflictOfInterest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConflictOfInterestRepository extends JpaRepository<ConflictOfInterest, Long> {
  // Tìm COI theo reviewer
  List<ConflictOfInterest> findByReviewerId(Long reviewerId);

  // Tìm COI theo submission
  List<ConflictOfInterest> findBySubmissionId(Long submissionId);

  // Tìm COI cụ thể giữa reviewer và submission
  Optional<ConflictOfInterest> findByReviewerIdAndSubmissionId(Long reviewerId, Long submissionId);

  List<ConflictOfInterest> findByReviewerIdAndActiveTrue(Long reviewerId);

  List<ConflictOfInterest> findBySubmissionIdAndActiveTrue(Long submissionId);
}
