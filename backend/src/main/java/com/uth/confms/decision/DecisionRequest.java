package com.uth.confms.decision;

import lombok.Data;

@Data
public class DecisionRequest {
    private Long submissionId;
    private String status; // VD: "ACCEPTED", "REJECTED"
    private String authorEmail;
    private String paperTitle;
    private String comment;
}