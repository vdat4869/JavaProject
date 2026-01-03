package com.uth.confms.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorResponse {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String affiliation;
    private String country;
    private Integer orderIndex;
    private Boolean isCorresponding;
    private Boolean isPresenting;
    private LocalDateTime createdAt;
}




