package com.uth.confms.cameraready.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO cho kết quả kiểm tra PDF.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResultDTO {

    private boolean passed;
    
    @Builder.Default
    private List<ValidationError> errors = new ArrayList<>();
    
    @Builder.Default
    private List<ValidationError> warnings = new ArrayList<>();
    
    private Integer pageCount;
    private String pageSize;
    private Long fileSizeBytes;

    public void addError(String code, String message) {
        errors.add(ValidationError.builder()
                .code(code)
                .message(message)
                .severity("ERROR")
                .build());
    }

    public void addWarning(String code, String message) {
        warnings.add(ValidationError.builder()
                .code(code)
                .message(message)
                .severity("WARNING")
                .build());
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String code;
        private String message;
        private String severity;
    }
}
