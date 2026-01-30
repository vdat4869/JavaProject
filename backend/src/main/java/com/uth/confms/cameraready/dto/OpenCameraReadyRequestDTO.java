package com.uth.confms.cameraready.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OpenCameraReadyRequestDTO {
    private LocalDateTime deadline; // Hạn chót nộp camera-ready
}
