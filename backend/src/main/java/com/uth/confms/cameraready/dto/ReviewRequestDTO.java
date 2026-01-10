package com.uth.confms.cameraready.dto;

import com.uth.confms.cameraready.entity.ReviewDecision;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO cho request duyệt bài nộp.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {

    @NotNull(message = "Quyết định không được để trống")
    private ReviewDecision decision;

    private String note;

    private UUID versionId;
}
