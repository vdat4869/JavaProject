package com.uth.confms.anhduc.exception;

import lombok.Getter;

/**
 * Exception khi vi phạm quy tắc nghiệp vụ.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Getter
public class BusinessRuleException extends RuntimeException {

    private final String errorCode;

    public BusinessRuleException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    // Pre-defined business rule exceptions
    public static BusinessRuleException cannotUpload() {
        return new BusinessRuleException("CANNOT_UPLOAD", 
                "Không thể tải lên ở trạng thái hiện tại");
    }

    public static BusinessRuleException cannotReview() {
        return new BusinessRuleException("CANNOT_REVIEW", 
                "Không thể duyệt ở trạng thái hiện tại");
    }

    public static BusinessRuleException copyrightNotConfirmed() {
        return new BusinessRuleException("COPYRIGHT_NOT_CONFIRMED", 
                "Chưa xác nhận bản quyền");
    }

    public static BusinessRuleException cameraReadyNotOpen() {
        return new BusinessRuleException("CAMERA_READY_NOT_OPEN", 
                "Chưa mở thời gian nộp camera-ready");
    }

    public static BusinessRuleException cameraReadyDeadlinePassed() {
        return new BusinessRuleException("CAMERA_READY_DEADLINE_PASSED", 
                "Đã quá hạn nộp camera-ready");
    }

    public static BusinessRuleException paperNotAccepted() {
        return new BusinessRuleException("PAPER_NOT_ACCEPTED", 
                "Bài báo chưa được chấp nhận");
    }

    public static BusinessRuleException validationFailed() {
        return new BusinessRuleException("VALIDATION_FAILED", 
                "PDF không vượt qua kiểm tra");
    }

    public static BusinessRuleException publicAccessDisabled() {
        return new BusinessRuleException("PUBLIC_ACCESS_DISABLED", 
                "Tính năng công khai bị tắt");
    }
}
