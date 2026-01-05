package com.uth.confms.pc.service;

import com.uth.confms.pc.entity.PCMember;
import com.uth.confms.pc.entity.enums.InvitationStatus;

/**
 * Service xu ly viec gui email moi PC member
 * Trong thuc te, service nay se tich hop voi email server (SMTP)
 * O day chi mo phong viec gui email
 */
public class EmailService {
    
    public boolean sendInvitation(PCMember pcMember) {
        if (pcMember == null || pcMember.getEmail() == null || pcMember.getEmail().isEmpty()) {
            System.out.println("❌ Khong the gui email: Email khong hop le");
            return false;
        }
        
        System.out.println("📧 Dang gui email moi den: " + pcMember.getEmail());
        System.out.println("   Nguoi nhan: " + pcMember.getName());
        System.out.println("   Noi dung: Moi ban tham gia Program Committee...");
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        pcMember.setInvitationStatus(InvitationStatus.PENDING);
        
        System.out.println("✅ Email da duoc gui thanh cong!");
        return true;
    }
    
    public void sendAcceptanceConfirmation(PCMember pcMember) {
        if (pcMember == null || pcMember.getEmail() == null) {
            return;
        }
        
        System.out.println("📧 Gui email xac nhan chap nhan loi moi den: " + pcMember.getEmail());
        System.out.println("   Cam on ban da chap nhan tham gia Program Committee!");
    }
    
    public void sendRejectionNotification(PCMember pcMember) {
        if (pcMember == null || pcMember.getEmail() == null) {
            return;
        }
        
        System.out.println("📧 Gui email thong bao tu choi den: " + pcMember.getEmail());
        System.out.println("   Chung toi rat tiec vi ban khong the tham gia lan nay.");
    }
    
    public void sendPaperAssignmentNotification(PCMember pcMember, String paperId) {
        if (pcMember == null || pcMember.getEmail() == null) {
            return;
        }
        
        System.out.println("📧 Gui email thong bao assignment den: " + pcMember.getEmail());
        System.out.println("   Ban da duoc assign de review paper: " + paperId);
    }
}
