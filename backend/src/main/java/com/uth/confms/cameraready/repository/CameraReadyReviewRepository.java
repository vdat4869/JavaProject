package com.uth.confms.cameraready.repository;

import com.uth.confms.cameraready.entity.CameraReadyReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository cho CameraReadyReview.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Repository
public interface CameraReadyReviewRepository extends JpaRepository<CameraReadyReview, UUID> {

    // Tìm tất cả reviews của một submission, sắp xếp theo thời gian mới nhất
    List<CameraReadyReview> findBySubmissionIdOrderByReviewedAtDesc(UUID submissionId);

    // Tìm tất cả reviews được thực hiện bởi một reviewer
    List<CameraReadyReview> findByReviewedBy(Long reviewerId);

    long countBySubmissionId(UUID submissionId);
}
