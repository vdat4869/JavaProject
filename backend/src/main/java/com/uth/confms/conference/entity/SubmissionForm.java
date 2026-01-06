package com.uth.confms.conference.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "submission_forms", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"conference_id", "field_name"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionForm {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conference_id", nullable = false)
    private Conference conference;
    
    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;
    
    @Column(name = "field_label", nullable = false)
    private String fieldLabel;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false, length = 50)
    private FieldType fieldType;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "field_options", columnDefinition = "jsonb")
    private Map<String, Object> fieldOptions;
    
    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = false;
    
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation_rules", columnDefinition = "jsonb")
    private Map<String, Object> validationRules;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum FieldType {
        TEXT, TEXTAREA, NUMBER, DATE, SELECT, CHECKBOX, FILE
    }
}

