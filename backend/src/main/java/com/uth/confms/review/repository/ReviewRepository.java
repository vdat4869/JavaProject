package com.uth.confms.review.repository;

import com.uth.confms.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findBySubmissionId(Long submissionId);
    List<Review> findByReviewerId(Long reviewerId);
    List<Review> findBySubmissionIdAndStatus(Long submissionId, Review.ReviewStatus status);
    List<Review> findByAssignmentId(Long assignmentId);
    Optional<Review> findByAssignmentIdAndReviewerId(Long assignmentId, Long reviewerId);
}

