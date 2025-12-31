package com.uth.confms.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSuggestionDTO {
    private Long reviewerId;
    private String reviewerEmail;
    private String reviewerName;
    private Double score;
    private String reason;
    private Boolean hasCOI;
}

