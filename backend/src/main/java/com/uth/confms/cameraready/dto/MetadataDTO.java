package com.uth.confms.cameraready.dto;

import com.uth.confms.cameraready.entity.PresentationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO cho metadata kỷ yếu.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataDTO {

    private UUID submissionId;
    private String doi; // Digital Object Identifier
    private Integer startPage; // Trang bắt đầu trong kỷ yếu
    private Integer endPage; // Trang kết thúc
    private PresentationType presentationType; // Hình thức trình bày (Oral, Poster)
    private Integer presentationDurationMinutes; // Thời lượng trình bày (phút)
    private Map<String, Object> extraMetadata; // Metadata bổ sung khác
    private LocalDateTime updatedAt;
}
