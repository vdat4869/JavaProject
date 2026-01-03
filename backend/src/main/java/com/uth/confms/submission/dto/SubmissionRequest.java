package com.uth.confms.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class SubmissionRequest {
    @NotNull(message = "Conference ID is required")
    private Long conferenceId;
    
    @NotNull(message = "Track ID is required")
    private Long trackId;
    
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;
    
    @Size(max = 5000, message = "Abstract must not exceed 5000 characters")
    private String abstractText;
    
    @Size(max = 500, message = "Keywords must not exceed 500 characters")
    private String keywords;
    
    private String notes;
    
    @NotNull(message = "At least one author is required")
    @Size(min = 1, message = "At least one author is required")
    private List<AuthorRequest> authors;
    
    @Data
    public static class AuthorRequest {
        private Long userId; // Optional: nếu user đã đăng ký
        
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        private String firstName;
        
        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        private String lastName;
        
        @Size(max = 200, message = "Email must not exceed 200 characters")
        private String email;
        
        @Size(max = 200, message = "Affiliation must not exceed 200 characters")
        private String affiliation;
        
        @Size(max = 100, message = "Country must not exceed 100 characters")
        private String country;
        
        @NotNull(message = "Order index is required")
        private Integer orderIndex;
        
        private Boolean isCorresponding = false;
        
        private Boolean isPresenting = false;
    }
}

