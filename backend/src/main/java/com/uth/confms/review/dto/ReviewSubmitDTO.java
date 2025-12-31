package com.uth.confms.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO cho request submit review
 * 
 * <p>DTO này chứa thông tin review cần submit:
 * <ul>
 *   <li>assignmentId - ID của assignment (required)</li>
 *   <li>summary - Tóm tắt review (required)</li>
 *   <li>strengths - Điểm mạnh (optional)</li>
 *   <li>weaknesses - Điểm yếu (optional)</li>
 *   <li>comments - Comments chi tiết (required)</li>
 *   <li>score - Điểm đánh giá (required): STRONG_ACCEPT, ACCEPT, WEAK_ACCEPT, BORDERLINE, WEAK_REJECT, REJECT, STRONG_REJECT</li>
 *   <li>isConfidential - Có phải confidential review không (required)</li>
 * </ul>
 * 
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Data
public class ReviewSubmitDTO {
    @NotNull(message = "Assignment ID is required")
    private Long assignmentId;
    
    @NotBlank(message = "Summary is required")
    private String summary;
    
    private String strengths;
    
    private String weaknesses;
    
    @NotBlank(message = "Comments are required")
    private String comments;
    
    @NotBlank(message = "Score is required")
    private String score; // ReviewScore enum value
    
    @NotNull(message = "Is confidential flag is required")
    private Boolean isConfidential;
}

