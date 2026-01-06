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

    Optional<CameraReadySubmission> findByPaperId(UUID paperId);

    boolean existsByPaperId(UUID paperId);

    List<CameraReadySubmission> findByConferenceId(UUID conferenceId);

    Page<CameraReadySubmission> findByConferenceId(UUID conferenceId, Pageable pageable);

    Page<CameraReadySubmission> findByConferenceIdAndStatus(UUID conferenceId, CameraReadyStatus status, Pageable pageable);

    List<CameraReadySubmission> findByConferenceIdAndStatus(UUID conferenceId, CameraReadyStatus status);

    Page<CameraReadySubmission> findByConferenceIdAndTrackId(UUID conferenceId, UUID trackId, Pageable pageable);

    @Query("SELECT s FROM CameraReadySubmission s WHERE s.conferenceId = :conferenceId " +
            "AND (:trackId IS NULL OR s.trackId = :trackId) " +
            "AND (:status IS NULL OR s.status = :status) " +
            "AND (:copyrightConfirmed IS NULL OR s.copyrightConfirmed = :copyrightConfirmed)")
    Page<CameraReadySubmission> findWithFilters(
            @Param("conferenceId") UUID conferenceId,
            @Param("trackId") UUID trackId,
            @Param("status") CameraReadyStatus status,
            @Param("copyrightConfirmed") Boolean copyrightConfirmed,
            Pageable pageable);

    long countByConferenceIdAndStatus(UUID conferenceId, CameraReadyStatus status);

    long countByConferenceIdAndCopyrightConfirmedTrue(UUID conferenceId);

    long countByConferenceId(UUID conferenceId);
}
