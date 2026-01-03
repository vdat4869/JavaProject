package com.uth.confms.submission.repository;

import com.uth.confms.submission.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository submission
 */
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
    // Tìm theo submitter
    Page<Submission> findBySubmitterId(Long submitterId, Pageable pageable);
    
    // Tìm theo conference
    Page<Submission> findByConferenceId(Long conferenceId, Pageable pageable);
    
    // Tìm theo conference và status
    Page<Submission> findByConferenceIdAndStatus(Long conferenceId, Submission.SubmissionStatus status, Pageable pageable);
    
    // Tìm theo submission number
    Optional<Submission> findBySubmissionNumber(String submissionNumber);
    
    // Tìm theo track
    Page<Submission> findByTrackId(Long trackId, Pageable pageable);
    
    // Đếm số submission theo conference và status
    long countByConferenceIdAndStatus(Long conferenceId, Submission.SubmissionStatus status);
    
    // Tìm submission của user trong conference
    @Query("SELECT s FROM Submission s WHERE s.submitterId = :userId AND s.conferenceId = :conferenceId")
    List<Submission> findBySubmitterAndConference(@Param("userId") Long userId, @Param("conferenceId") Long conferenceId);
    
    // Tìm submission của user trong conference với pagination
    Page<Submission> findBySubmitterIdAndConferenceId(Long submitterId, Long conferenceId, Pageable pageable);
    
    // Kiểm tra submission có phải của user không
    boolean existsByIdAndSubmitterId(Long id, Long submitterId);
}

