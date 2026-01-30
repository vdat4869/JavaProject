package com.uth.confms.decision.dto;

import java.time.LocalDateTime;

public class DecisionHistoryDTO {
    private Long id;
    private Long decisionId;
    private Long changedBy; // ID người thay đổi
    private String changedByName; // Tên người thay đổi
    private String changeType; // Loại thay đổi
    private String oldValue; // Giá trị cũ
    private String newValue; // Giá trị mới
    private String fieldName; // Tên trường thay đổi
    private String description; // Mô tả
    private LocalDateTime changedAt; // Thời gian thay đổi

    public DecisionHistoryDTO() {
    }

    public DecisionHistoryDTO(Long id, Long decisionId, Long changedBy, String changedByName, String changeType,
            String oldValue, String newValue, String fieldName, String description, LocalDateTime changedAt) {
        this.id = id;
        this.decisionId = decisionId;
        this.changedBy = changedBy;
        this.changedByName = changedByName;
        this.changeType = changeType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.fieldName = fieldName;
        this.description = description;
        this.changedAt = changedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(Long decisionId) {
        this.decisionId = decisionId;
    }

    public Long getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(Long changedBy) {
        this.changedBy = changedBy;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
