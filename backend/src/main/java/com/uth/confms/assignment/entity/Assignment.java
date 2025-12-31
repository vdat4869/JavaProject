package com.uth.confms.assignment.entity;

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
 * Entity đại diện cho assignment (phân công reviewer cho submission)
 * 
 * <p>Một assignment liên kết một reviewer với một submission để review.
 * Assignment có các trạng thái:
 * <ul>
 *   <li>ASSIGNED - Đã được phân công, chờ reviewer accept/decline</li>
 *   <li>ACCEPTED - Reviewer đã chấp nhận assignment</li>
 *   <li>DECLINED - Reviewer đã từ chối assignment</li>
 *   <li>COMPLETED - Review đã hoàn thành</li>
 * </ul>
 * 
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Entity
@Table(name = "assignments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long submissionId;
    
    @Column(nullable = false)
    private Long reviewerId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime assignedAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    /**
     * Enum định nghĩa các trạng thái của assignment
     */
    public enum AssignmentStatus {
        /** Đã được phân công, chờ reviewer accept/decline */
        ASSIGNED,
        /** Reviewer đã chấp nhận assignment */
        ACCEPTED,
        /** Reviewer đã từ chối assignment */
        DECLINED,
        /** Review đã hoàn thành */
        COMPLETED
    }
}

