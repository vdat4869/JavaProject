package com.uth.confms.submission.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity submission
 */
@Entity
@Table(name = "submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long conferenceId;
    
    @Column(nullable = false)
    private Long trackId;
    
    @Column(nullable = false)
    private Long submitterId; // Tác giả chính (corresponding author)
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String abstractText;
    
    @Column(columnDefinition = "TEXT")
    private String keywords;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.DRAFT;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubmissionType type = SubmissionType.RESEARCH_PAPER;
    
    @Column(length = 50)
    private String submissionNumber; // Format: CONF-YYYY-XXX
    
    @Column(columnDefinition = "TEXT")
    private String notes; // Ghi chú của tác giả
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isBlind = true; // Double-blind review
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isWithdrawn = false;
    
    @Column
    private LocalDateTime withdrawnAt;
    
    @Column(length = 500)
    private String withdrawReason;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @Column
    private LocalDateTime submittedAt; // Thời điểm submit chính thức
    
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Author> authors = new ArrayList<>();
    
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SubmissionFile> files = new ArrayList<>();
    
    public enum SubmissionStatus {
        DRAFT,           // Đang soạn thảo
        SUBMITTED,       // Đã nộp
        UNDER_REVIEW,    // Đang phản biện
        REVISION,        // Yêu cầu chỉnh sửa
        ACCEPTED,        // Chấp nhận
        REJECTED,        // Từ chối
        WITHDRAWN        // Rút bài
    }
    
    public enum SubmissionType {
        RESEARCH_PAPER,  // Bài nghiên cứu
        SHORT_PAPER,     // Bài ngắn
        POSTER,          // Poster
        DEMO             // Demo
    }
}

