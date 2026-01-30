package com.uth.confms.ai.controller;

import com.uth.confms.ai.dto.*;
import com.uth.confms.ai.service.ChairAIService;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho các tính năng AI dành cho Chair.
 *
 * <p>
 * Các endpoints:
 * <ul>
 * <li>POST /ai/chair/email-draft - Soạn email thông báo
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/ai/chair")
public class ChairAIController {

    private final ChairAIService chairAIService;
    private final UserRepository userRepository;

    public ChairAIController(ChairAIService chairAIService, UserRepository userRepository) {
        this.chairAIService = chairAIService;
        this.userRepository = userRepository;
    }

    /**
     * Soạn thảo email thông báo.
     * Chair PHẢI review và chỉnh sửa trước khi gửi - KHÔNG tự động gửi.
     */
    @PostMapping("/email-draft")
    @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmailDraftResponse>> draftEmail(
            @Valid @RequestBody EmailDraftRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        EmailDraftResponse response = chairAIService.draftEmail(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lấy ID người dùng từ thông tin xác thực.
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
