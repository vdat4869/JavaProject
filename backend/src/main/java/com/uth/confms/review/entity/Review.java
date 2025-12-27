package com.uth.confms.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long assignmentId;
    
    @Column(nullable = false)
    private Long submissionId;
    
    @Column(nullable = false)
    private Long reviewerId;
    
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    @Column(columnDefinition = "TEXT")
    private String strengths;
    
    @Column(columnDefinition = "TEXT")
    private String weaknesses;
    
    @Column(columnDefinition = "TEXT")
    private String comments;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReviewScore score;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.DRAFT;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isConfidential = false;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime submittedAt;
    
    public enum ReviewScore {
        STRONG_ACCEPT, ACCEPT, WEAK_ACCEPT, BORDERLINE, WEAK_REJECT, REJECT, STRONG_REJECT
    }
    
    public enum ReviewStatus {
        DRAFT, SUBMITTED
    }
}

