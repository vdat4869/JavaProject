package com.uth.confms.decision;

import com.uth.confms.notification.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DecisionService {

    @Autowired
    private EmailService emailService;

    // Xử lý 1 bài báo
    public void processDecision(DecisionRequest request) {
        // 1. Logic cập nhật Database (Ví dụ: submissionRepo.updateStatus(...))
        
        // 2. Gửi mail thông báo ngay lập tức
        emailService.sendSimpleEmail(
            request.getAuthorEmail(), 
            "Thông báo kết quả bài báo: " + request.getPaperTitle(),
            "Trạng thái: " + request.getStatus() + "\nLời nhắn: " + request.getComment()
        );
    }

    // Xử lý gửi hàng loạt
    public void sendBulkDecisions(List<Long> submissionIds) {
        // Giả sử bạn lấy danh sách bài báo từ DB dựa trên IDs
        // Sau đó chạy vòng lặp gọi emailService.sendSimpleEmail
        // Nhờ có @Async ở EmailService, vòng lặp này sẽ chạy cực nhanh
    }
}