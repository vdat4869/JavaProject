package com.uth.confms.cameraready.repository;

import com.uth.confms.cameraready.entity.CameraReadyMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository cho CameraReadyMetadata.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Repository
public interface CameraReadyMetadataRepository extends JpaRepository<CameraReadyMetadata, UUID> {

    Optional<CameraReadyMetadata> findBySubmissionId(UUID submissionId);

    boolean existsByDoi(String doi);

    Optional<CameraReadyMetadata> findByDoi(String doi);
}
