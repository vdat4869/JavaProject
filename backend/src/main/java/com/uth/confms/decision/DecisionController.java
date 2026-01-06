package com.uth.confms.decision;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decisions")
@CrossOrigin(origins = "*") // Cho phép Frontend gọi API
public class DecisionController {

    @Autowired
    private DecisionService decisionService;

    /**
     * API đưa ra quyết định cho một bài báo cụ thể
     * POST /api/decisions/make-decision
     */
    @PostMapping("/make-decision")
    public ResponseEntity<String> makeDecision(@RequestBody DecisionRequest request) {
        decisionService.processDecision(request);
        return ResponseEntity.ok("Quyết định đã được lưu và email đã được gửi!");
    }

    /**
     * API gửi email hàng loạt cho tất cả bài báo đã có kết quả
     * POST /api/decisions/bulk-notify
     */
    @PostMapping("/bulk-notify")
    public ResponseEntity<String> sendBulkEmails(@RequestBody List<Long> submissionIds) {
        decisionService.sendBulkDecisions(submissionIds);
        return ResponseEntity.ok("Đang bắt đầu tiến trình gửi email hàng loạt...");
    }
}