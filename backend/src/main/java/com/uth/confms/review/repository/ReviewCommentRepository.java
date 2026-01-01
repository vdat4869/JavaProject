package com.uth.confms.review.repository;

import com.uth.confms.review.entity.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {
    List<ReviewComment> findBySubmissionId(Long submissionId);
    List<ReviewComment> findBySubmissionIdAndIsInternalTrue(Long submissionId);
    List<ReviewComment> findByReviewerId(Long reviewerId);
}

