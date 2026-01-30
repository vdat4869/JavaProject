package com.uth.confms.cameraready.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request xác nhận bản quyền.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopyrightConfirmRequestDTO {

    @NotNull(message = "Phải xác nhận bản quyền")
    private Boolean confirmed; // Xác nhận đã đọc và đồng ý

    private String agreement; // Nội dung thỏa thuận (nếu có)
}
