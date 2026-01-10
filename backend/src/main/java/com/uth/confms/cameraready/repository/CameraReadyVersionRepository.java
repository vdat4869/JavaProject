package com.uth.confms.cameraready.repository;

import com.uth.confms.cameraready.entity.CameraReadyVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository cho CameraReadyVersion.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Repository
public interface CameraReadyVersionRepository extends JpaRepository<CameraReadyVersion, UUID> {

    List<CameraReadyVersion> findBySubmissionIdOrderByVersionNumberDesc(UUID submissionId);

    Optional<CameraReadyVersion> findBySubmissionIdAndVersionNumber(UUID submissionId, Integer versionNumber);

    Optional<CameraReadyVersion> findFirstBySubmissionIdOrderByVersionNumberDesc(UUID submissionId);

    long countBySubmissionId(UUID submissionId);

    boolean existsBySubmissionIdAndChecksumSha256(UUID submissionId, String checksumSha256);
}
