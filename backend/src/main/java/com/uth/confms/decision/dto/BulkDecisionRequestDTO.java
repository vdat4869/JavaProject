package com.uth.confms.decision.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BulkDecisionRequestDTO {
    @NotEmpty(message = "Submission list cannot be empty")
    private List<Long> submissionIds; // Danh sách ID submission

    @NotNull(message = "Decision type is required")
    private String type; // Loại decision (ACCEPT/REJECT)

    private String comments; // Nhận xét chung

    private Boolean sendNotification = false; // Gửi thông báo ngay

    public List<Long> getSubmissionIds() {
        return submissionIds;
    }

    public void setSubmissionIds(List<Long> submissionIds) {
        this.submissionIds = submissionIds;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Boolean getSendNotification() {
        return sendNotification;
    }

    public void setSendNotification(Boolean sendNotification) {
        this.sendNotification = sendNotification;
    }
}
