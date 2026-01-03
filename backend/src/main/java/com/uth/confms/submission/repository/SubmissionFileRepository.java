package com.uth.confms.submission.repository;

import com.uth.confms.submission.entity.SubmissionFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository file submission
 */
@Repository
public interface SubmissionFileRepository extends JpaRepository<SubmissionFile, Long> {
    List<SubmissionFile> findBySubmissionId(Long submissionId);
    
    List<SubmissionFile> findBySubmissionIdAndCategory(Long submissionId, SubmissionFile.FileCategory category);
    
    Optional<SubmissionFile> findBySubmissionIdAndCategoryAndIsLatestTrue(
        Long submissionId, 
        SubmissionFile.FileCategory category
    );
    
    void deleteBySubmissionId(Long submissionId);
}

