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
public static BusinessRuleException alreadyApproved() {
    return new BusinessRuleException("ALREADY_APPROVED", 
            "Bài nộp đã được phê duyệt, không thể thay đổi");
    }

public static BusinessRuleException deadlineNotSet() {
    return new BusinessRuleException("DEADLINE_NOT_SET", 
            "Chưa thiết lập deadline nộp camera-ready");
    }

public static BusinessRuleException duplicateFile() {
    return new BusinessRuleException("DUPLICATE_FILE", 
            "File này đã được tải lên trước đó");
    }

public static BusinessRuleException maxVersionsExceeded(int maxVersions) {
    return new BusinessRuleException("MAX_VERSIONS_EXCEEDED", 
            String.format("Đã vượt quá số phiên bản tối đa cho phép (%d)", maxVersions));
    }

public static BusinessRuleException noVersionUploaded() {
    return new BusinessRuleException("NO_VERSION_UPLOADED", 
            "Chưa có phiên bản nào được tải lên");
    }

public static BusinessRuleException trackNotInConference() {
    return new BusinessRuleException("TRACK_NOT_IN_CONFERENCE", 
            "Track không thuộc hội nghị này");
    }
}
