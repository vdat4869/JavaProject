package com.uth.confms.submission.repository;

import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.entity.SubmissionAuthor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionAuthorRepository extends JpaRepository<SubmissionAuthor, Long> {
  List<SubmissionAuthor> findBySubmission(Submission submission);

  List<SubmissionAuthor> findBySubmissionId(Long submissionId);

  List<SubmissionAuthor> findBySubmissionIdIn(List<Long> submissionIds);

  void deleteBySubmission(Submission submission);
}
