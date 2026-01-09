package com.uth.confms.cameraready.repository;

import com.uth.confms.cameraready.entity.CameraReadySubmission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CameraReadyRepository extends JpaRepository<CameraReadySubmission, Long> {
  Optional<CameraReadySubmission> findBySubmissionId(Long submissionId);

  List<CameraReadySubmission> findByValidationStatus(CameraReadySubmission.ValidationStatus status);

  List<CameraReadySubmission> findByApprovedTrue();

  List<CameraReadySubmission> findByApprovedFalse();
}
