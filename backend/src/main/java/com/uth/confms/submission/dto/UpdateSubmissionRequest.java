package com.uth.confms.submission.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateSubmissionRequest {
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;
    
    @Size(max = 5000, message = "Abstract must not exceed 5000 characters")
    private String abstractText;
    
    @Size(max = 500, message = "Keywords must not exceed 500 characters")
    private String keywords;
    
    private String notes;
    
    private List<SubmissionRequest.AuthorRequest> authors;
}




