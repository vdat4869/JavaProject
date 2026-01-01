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

/**
 * Entity đại diện cho review (đánh giá bài nộp)
 * 
 * <p>Review được tạo bởi reviewer sau khi accept assignment.
 * Review có các trạng thái:
 * <ul>
 *   <li>DRAFT - Đang soạn thảo, chưa submit</li>
 *   <li>SUBMITTED - Đã submit, không thể chỉnh sửa</li>
 * </ul>
 * 
 * <p>Review có các scores từ STRONG_ACCEPT đến STRONG_REJECT.
 * Review có thể là confidential (chỉ chair/PC thấy) hoặc public (author thấy).
 * 
 * @author UTH-ConfMS Team
 * @version 1.0
 */
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
    
    /**
     * Enum định nghĩa các điểm đánh giá (score) của review
     */
    public enum ReviewScore {
        /** Chấp nhận mạnh mẽ */
        STRONG_ACCEPT,
        /** Chấp nhận */
        ACCEPT,
        /** Chấp nhận yếu */
        WEAK_ACCEPT,
        /** Ranh giới (có thể chấp nhận hoặc từ chối) */
        BORDERLINE,
        /** Từ chối yếu */
        WEAK_REJECT,
        /** Từ chối */
        REJECT,
        /** Từ chối mạnh mẽ */
        STRONG_REJECT
    }
    
    /**
     * Enum định nghĩa các trạng thái của review
     */
    public enum ReviewStatus {
        /** Đang soạn thảo, chưa submit */
        DRAFT,
        /** Đã submit, không thể chỉnh sửa */
        SUBMITTED
    }
}

