package com.uth.confms.review.repository;

import com.uth.confms.review.entity.Rebuttal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RebuttalRepository extends JpaRepository<Rebuttal, Long> {
    Optional<Rebuttal> findBySubmissionId(Long submissionId);
    List<Rebuttal> findByAuthorId(Long authorId);
    List<Rebuttal> findBySubmissionIdAndStatus(Long submissionId, Rebuttal.RebuttalStatus status);
}

