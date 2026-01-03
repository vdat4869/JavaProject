package com.uth.confms.submission.dto;

import com.uth.confms.submission.entity.Submission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {
    private Long id;
    private Long conferenceId;
    private Long trackId;
    private Long submitterId;
    private String title;
    private String abstractText;
    private String keywords;
    private Submission.SubmissionStatus status;
    private Submission.SubmissionType type;
    private String submissionNumber;
    private String notes;
    private Boolean isBlind;
    private Boolean isWithdrawn;
    private LocalDateTime withdrawnAt;
    private String withdrawReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime submittedAt;
    
    @Builder.Default
    private List<AuthorResponse> authors = new ArrayList<>();
    
    @Builder.Default
    private List<FileResponse> files = new ArrayList<>();
}




