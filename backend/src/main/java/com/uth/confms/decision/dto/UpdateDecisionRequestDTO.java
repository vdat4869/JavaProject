package com.uth.confms.decision.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO cho request cập nhật decision
 *
 * <p>
 * DTO này chứa thông tin decision cần cập nhật:
 *
 * <ul>
 * <li>type - Loại decision mới (optional): ACCEPT, REJECT, CONDITIONAL_ACCEPT
 * <li>comments - Comments mới (optional)
 * <li>reason - Lý do thay đổi (required cho audit trail)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class UpdateDecisionRequestDTO {
    private String type; // ACCEPT, REJECT, CONDITIONAL_ACCEPT (optional)

    private String comments; // Nhận xét mới (optional)

    @NotNull(message = "Reason for change is required")
    private String reason; // Lý do thay đổi (bắt buộc để audit)

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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
