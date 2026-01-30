package com.uth.confms.submission.repository;

import com.uth.confms.submission.entity.Submission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho Submission entity
 *
 * <p>
 * Repository này cung cấp các methods để truy vấn Submission:
 *
 * <ul>
 * <li>findByAuthorId - Lấy tất cả submissions của một author
 * <li>findByConferenceId - Lấy tất cả submissions của một conference
 * <li>findByConferenceIdAndAuthorId - Lấy submissions của author trong một
 * conference
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
  /**
   * Lấy tất cả submissions của một author
   *
   * @param authorId ID của author
   * @return Danh sách submissions của author
   */
  // Tìm submission theo author ID
  List<Submission> findByAuthorId(Long authorId);

  /**
   * Lấy tất cả submissions của một conference
   *
   * @param conferenceId ID của conference
   * @return Danh sách submissions của conference
   */
  // Tìm submission theo conference ID
  List<Submission> findByConferenceId(Long conferenceId);

  /**
   * Lấy submissions của một author trong một conference cụ thể
   *
   * @param conferenceId ID của conference
   * @param authorId     ID của author
   * @return Danh sách submissions
   */
  // Tìm submission theo conference và author
  List<Submission> findByConferenceIdAndAuthorId(Long conferenceId, Long authorId);
}
