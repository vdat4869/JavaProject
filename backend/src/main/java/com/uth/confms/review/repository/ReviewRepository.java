package com.uth.confms.review.repository;

import com.uth.confms.review.entity.Review;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
  // Tìm reviews bài báo
  List<Review> findBySubmissionId(Long submissionId);

  // Tìm reviews của reviewer
  List<Review> findByReviewerId(Long reviewerId);

  List<Review> findByReviewerIdIn(Set<Long> reviewerIds);

  List<Review> findBySubmissionIdAndStatus(Long submissionId, Review.ReviewStatus status);

  // Tìm reviews theo phân công
  List<Review> findByAssignmentId(Long assignmentId);

  List<Review> findBySubmissionIdIn(List<Long> submissionIds);

  List<Review> findByAssignmentIdIn(List<Long> assignmentIds);

  Optional<Review> findByAssignmentIdAndReviewerId(Long assignmentId, Long reviewerId);
}
