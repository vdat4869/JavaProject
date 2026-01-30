package com.uth.confms.submission.repository;

import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.entity.SubmissionFile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionFileRepository extends JpaRepository<SubmissionFile, Long> {
  List<SubmissionFile> findBySubmission(Submission submission);

  List<SubmissionFile> findBySubmissionId(Long submissionId);

  List<SubmissionFile> findBySubmissionIdIn(List<Long> submissionIds);

  Optional<SubmissionFile> findBySubmissionAndIsCurrentTrue(Submission submission);

  Optional<SubmissionFile> findBySubmissionIdAndIsCurrentTrue(Long submissionId);

  Integer countBySubmission(Submission submission);
}
