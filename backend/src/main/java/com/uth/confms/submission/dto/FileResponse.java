package com.uth.confms.submission.dto;

import com.uth.confms.submission.entity.SubmissionFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private SubmissionFile.FileCategory category;
    private Integer version;
    private Boolean isLatest;
    private String downloadUrl; // URL để download file
    private LocalDateTime uploadedAt;
    private Long uploadedBy;
}




