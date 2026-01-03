package com.uth.confms.submission.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity tác giả  
 */
@Entity
@Table(name = "authors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;
    
    @Column(nullable = false)
    private Long userId; // ID của user trong hệ thống (nếu đã đăng ký)
    
    @Column(nullable = false, length = 100)
    private String firstName;
    
    @Column(nullable = false, length = 100)
    private String lastName;
    
    @Column(length = 200)
    private String email;
    
    @Column(length = 200)
    private String affiliation; // Đơn vị công tác
    
    @Column(length = 100)
    private String country;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer orderIndex = 0; // Thứ tự tác giả (1 = first author)
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isCorresponding = false; // Tác giả liên hệ
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isPresenting = false; // Tác giả trình bày
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

