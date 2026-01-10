package com.uth.confms.cameraready.entity;

/**
 * Trạng thái của bài nộp camera-ready.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
public enum CameraReadyStatus {
    
    /**
     * Chưa mở thời gian nộp camera-ready.
     */
    NOT_OPEN,
    
    /**
     * Đang mở, chờ tác giả tải lên lần đầu.
     */
    OPEN,
    
    /**
     * Đã nộp, đang chờ Chair duyệt.
     */
    SUBMITTED,
    
    /**
     * Chair yêu cầu chỉnh sửa.
     */
    NEED_FIX,
    
    /**
     * Đã phê duyệt, sẵn sàng vào kỷ yếu.
     */
    APPROVED,
    
    /**
     * Thời gian nộp đã kết thúc.
     */
    CLOSED;

    /**
     * Kiểm tra có thể tải lên phiên bản mới không.
     */
    public boolean canUpload() {
        return this == OPEN || this == NEED_FIX;
    }

    /**
     * Kiểm tra có thể duyệt không.
     */
    public boolean canReview() {
        return this == SUBMITTED || this == NEED_FIX;
    }

    /**
     * Kiểm tra có thể chuyển sang trạng thái mới không.
     */
    public boolean canTransitionTo(CameraReadyStatus target) {
        return switch (this) {
            case NOT_OPEN -> target == OPEN;
            case OPEN -> target == SUBMITTED || target == CLOSED;
            case SUBMITTED -> target == APPROVED || target == NEED_FIX || target == CLOSED;
            case NEED_FIX -> target == SUBMITTED || target == CLOSED;
            case APPROVED -> target == CLOSED;
            case CLOSED -> false;
        };
    }
}
