package com.uth.confms.assignment.repository;

import com.uth.confms.assignment.entity.Assignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository cho Assignment entity
 *
 * <p>
 * Repository này cung cấp các methods để truy vấn Assignment:
 *
 * <ul>
 * <li>findBySubmissionId - Lấy assignments của một submission
 * <li>findByReviewerId - Lấy assignments của một reviewer
 * <li>findBySubmissionIdAndStatus - Lấy assignments theo submission và status
 * <li>findByReviewerIdAndStatus - Lấy assignments theo reviewer và status
 * <li>existsBySubmissionIdAndReviewerId - Kiểm tra assignment đã tồn tại
 * <li>countBySubmissionId - Đếm số assignments của submission
 * <li>countByReviewerIdAndStatus - Đếm số assignments của reviewer theo status
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
  /**
   * Lấy tất cả assignments của một submission
   *
   * @param submissionId ID của submission
   * @return Danh sách assignments
   */
  List<Assignment> findBySubmissionId(Long submissionId);

  List<Assignment> findBySubmissionIdIn(List<Long> submissionIds);

  /**
   * Lấy tất cả assignments của một reviewer
   *
   * @param reviewerId ID của reviewer
   * @return Danh sách assignments
   */
  List<Assignment> findByReviewerId(Long reviewerId);

  /**
   * Lấy assignments của submission theo status
   *
   * @param submissionId ID của submission
   * @param status       Trạng thái assignment
   * @return Danh sách assignments
   */
  List<Assignment> findBySubmissionIdAndStatus(
      Long submissionId, Assignment.AssignmentStatus status);

  /**
   * Lấy assignments của reviewer theo status
   *
   * @param reviewerId ID của reviewer
   * @param status     Trạng thái assignment
   * @return Danh sách assignments
   */
  List<Assignment> findByReviewerIdAndStatus(Long reviewerId, Assignment.AssignmentStatus status);

  /**
   * Kiểm tra assignment đã tồn tại cho submission và reviewer
   *
   * @param submissionId ID của submission
   * @param reviewerId   ID của reviewer
   * @return true nếu đã tồn tại, false nếu chưa
   */
  boolean existsBySubmissionIdAndReviewerId(Long submissionId, Long reviewerId);

  /**
   * Đếm số assignments của một submission
   *
   * @param submissionId ID của submission
   * @return Số lượng assignments
   */
  long countBySubmissionId(Long submissionId);

  /**
   * Đếm số assignments của reviewer theo status
   *
   * @param reviewerId ID của reviewer
   * @param status     Trạng thái assignment
   * @return Số lượng assignments
   */
  long countByReviewerIdAndStatus(Long reviewerId, Assignment.AssignmentStatus status);

  @Query("SELECT a FROM Assignment a WHERE a.submissionId IN (SELECT s.id FROM Submission s WHERE s.conferenceId = :conferenceId)")
  List<Assignment> findByConferenceId(@Param("conferenceId") Long conferenceId);

  @Query("SELECT a FROM Assignment a WHERE a.reviewerId = :reviewerId AND a.submissionId IN (SELECT s.id FROM Submission s WHERE s.conferenceId = :conferenceId)")
  List<Assignment> findByReviewerIdAndConferenceId(@Param("reviewerId") Long reviewerId,
      @Param("conferenceId") Long conferenceId);
}
