package com.uth.confms.pc.entity.enums;

/**
 * Enum dinh nghia cac trang thai cua loi moi PC member
 * PENDING: Loi moi da duoc gui nhung chua co phan hoi
 * ACCEPTED: PC member da chap nhan loi moi
 * REJECTED: PC member da tu choi loi moi
 */
public enum InvitationStatus {
    PENDING,    // Dang cho phan hoi
    ACCEPTED,   // Da chap nhan
    REJECTED    // Da tu choi
}

