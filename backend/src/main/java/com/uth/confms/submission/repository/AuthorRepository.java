package com.uth.confms.submission.repository;

import com.uth.confms.submission.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository tác giả
 */
@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    List<Author> findBySubmissionId(Long submissionId);
    
    void deleteBySubmissionId(Long submissionId);
}

