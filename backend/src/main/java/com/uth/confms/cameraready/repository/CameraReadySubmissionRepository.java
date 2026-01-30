package com.uth.confms.cameraready.repository;

import com.uth.confms.cameraready.entity.CameraReadyStatus;
import com.uth.confms.cameraready.entity.CameraReadySubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository cho CameraReadySubmission.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Repository
public interface CameraReadySubmissionRepository extends JpaRepository<CameraReadySubmission, UUID> {

        Optional<CameraReadySubmission> findByPaperId(Long paperId);

        boolean existsByPaperId(Long paperId);

        List<CameraReadySubmission> findByConferenceId(Long conferenceId);

        Page<CameraReadySubmission> findByConferenceId(Long conferenceId, Pageable pageable);

        Page<CameraReadySubmission> findByConferenceIdAndStatus(Long conferenceId, CameraReadyStatus status,
                        Pageable pageable);

        List<CameraReadySubmission> findByConferenceIdAndStatus(Long conferenceId, CameraReadyStatus status);

        Page<CameraReadySubmission> findByConferenceIdAndTrackId(Long conferenceId, Long trackId, Pageable pageable);

        // Query phức tạp để lọc submission theo nhiều tiêu chí
        @Query("SELECT s FROM CameraReadySubmission s WHERE s.conferenceId = :conferenceId " +
                        "AND (:trackId IS NULL OR s.trackId = :trackId) " +
                        "AND (:status IS NULL OR s.status = :status) " +
                        "AND (:copyrightConfirmed IS NULL OR s.copyrightConfirmed = :copyrightConfirmed)")
        Page<CameraReadySubmission> findWithFilters(
                        @Param("conferenceId") Long conferenceId,
                        @Param("trackId") Long trackId,
                        @Param("status") CameraReadyStatus status,
                        @Param("copyrightConfirmed") Boolean copyrightConfirmed,
                        Pageable pageable);

        long countByConferenceIdAndStatus(Long conferenceId, CameraReadyStatus status);

        long countByConferenceIdAndCopyrightConfirmedTrue(Long conferenceId);

        long countByConferenceId(Long conferenceId);

        // Đếm số lượng theo status để làm thống kê
        @Query("SELECT s.status, COUNT(s) FROM CameraReadySubmission s WHERE s.conferenceId = :conferenceId GROUP BY s.status")
        List<Object[]> countByConferenceIdGroupByStatus(@Param("conferenceId") Long conferenceId);
}
