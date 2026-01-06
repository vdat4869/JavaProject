package com.uth.confms.cameraready.dto;

import com.uth.confms.cameraready.entity.PresentationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO cho request cập nhật metadata.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataUpdateRequestDTO {

    private String doi;
    private Integer startPage;
    private Integer endPage;
    private PresentationType presentationType;
    private Integer presentationDurationMinutes;
    private Map<String, Object> extraMetadata;
}
