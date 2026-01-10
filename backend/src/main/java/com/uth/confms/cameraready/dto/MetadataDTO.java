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
    private String doi;
    private Integer startPage;
    private Integer endPage;
    private PresentationType presentationType;
    private Integer presentationDurationMinutes;
    private Map<String, Object> extraMetadata;
    private LocalDateTime updatedAt;
}
