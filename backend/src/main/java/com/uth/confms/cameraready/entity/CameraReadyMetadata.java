package com.uth.confms.cameraready.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entity đại diện cho metadata của bài nộp trong kỷ yếu (proceedings).
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Entity
@Table(name = "camera_ready_metadata")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CameraReadyMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private CameraReadySubmission submission;

    @Column(name = "doi", unique = true, length = 100)
    private String doi;

    @Column(name = "start_page")
    private Integer startPage;

    @Column(name = "end_page")
    private Integer endPage;

    @Enumerated(EnumType.STRING)
    @Column(name = "presentation_type", length = 20)
    private PresentationType presentationType;

    @Column(name = "presentation_duration_minutes")
    private Integer presentationDurationMinutes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_metadata", columnDefinition = "jsonb")
    private Map<String, Object> extraMetadata;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
