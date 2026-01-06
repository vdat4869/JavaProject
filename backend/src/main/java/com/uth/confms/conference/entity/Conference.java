package com.uth.confms.conference.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "conferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(unique = true, length = 50)
    private String acronym;
    
    @Column(nullable = false)
    private Integer year;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "submission_deadline")
    private LocalDateTime submissionDeadline;
    
    @Column(name = "review_deadline")
    private LocalDateTime reviewDeadline;
    
    @Column(name = "camera_ready_deadline")
    private LocalDateTime cameraReadyDeadline;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "review_mode", nullable = false, length = 20)
    @Builder.Default
    private ReviewMode reviewMode = ReviewMode.DOUBLE_BLIND;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ConferenceStatus status = ConferenceStatus.DRAFT;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
    
    public enum ReviewMode {
        SINGLE_BLIND, DOUBLE_BLIND
    }
    
    public enum ConferenceStatus {
        DRAFT, OPEN, CLOSED, COMPLETED
    }
}

