package com.uth.confms.cameraready.repository;

import com.uth.confms.cameraready.entity.CameraReadyMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

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

    List<CameraReadyMetadata> findBySubmissionIdIn(List<UUID> submissionIds);

    boolean existsByDoi(String doi); // Kiểm tra DOI đã tồn tại chưa

    Optional<CameraReadyMetadata> findByDoi(String doi); // Tìm metadata theo DOI
}
