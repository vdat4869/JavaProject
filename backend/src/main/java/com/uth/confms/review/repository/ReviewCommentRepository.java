package com.uth.confms.review.repository;

import com.uth.confms.review.entity.ReviewComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {
  // Lấy tất cả comments của submission
  List<ReviewComment> findBySubmissionId(Long submissionId);

  // Lấy comments internal (chỉ thảo luận nội bộ)
  List<ReviewComment> findBySubmissionIdAndIsInternalTrue(Long submissionId);

  List<ReviewComment> findByReviewerId(Long reviewerId);
}
