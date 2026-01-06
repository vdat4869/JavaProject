package com.uth.confms.conference.repository;

import com.uth.confms.conference.entity.Conference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConferenceRepository extends JpaRepository<Conference, Long> {
    
    List<Conference> findByIsDeletedFalse();
    
    Optional<Conference> findByIdAndIsDeletedFalse(Long id);
    
    Optional<Conference> findByAcronymAndIsDeletedFalse(String acronym);
    
    List<Conference> findByStatusAndIsDeletedFalse(Conference.ConferenceStatus status);
    
    @Query("SELECT c FROM Conference c WHERE c.status = :status AND c.isDeleted = false ORDER BY c.createdAt DESC")
    List<Conference> findActiveConferences(@Param("status") Conference.ConferenceStatus status);
    
    boolean existsByAcronymAndIsDeletedFalse(String acronym);
}

