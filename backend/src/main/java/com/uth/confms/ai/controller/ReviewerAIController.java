package com.uth.confms.ai.controller;

import com.uth.confms.ai.dto.*;
import com.uth.confms.ai.service.ReviewerAIService;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho các tính năng AI dành cho Reviewer/PC.
 *
 * <p>
 * Các endpoints:
 * <ul>
 * <li>POST /ai/pc/neutral-summary - Tạo tóm tắt trung lập
 * <li>POST /ai/pc/key-points - Trích xuất key points
 * <li>POST /ai/assignment/similarity-hint - Gợi ý độ tương đồng
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/ai")
public class ReviewerAIController {

    private final ReviewerAIService reviewerAIService;
    private final UserRepository userRepository;

    public ReviewerAIController(ReviewerAIService reviewerAIService, UserRepository userRepository) {
        this.reviewerAIService = reviewerAIService;
        this.userRepository = userRepository;
    }

    /**
     * Tạo tóm tắt trung lập cho PC bidding.
     */
    @PostMapping("/pc/neutral-summary")
    @PreAuthorize("hasRole('PC')")
    public ResponseEntity<ApiResponse<NeutralSummaryResponse>> neutralSummary(
            @Valid @RequestBody NeutralSummaryRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        NeutralSummaryResponse response = reviewerAIService.generateNeutralSummary(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Trích xuất key points từ abstract.
     */
    @PostMapping("/pc/key-points")
    @PreAuthorize("hasRole('PC')")
    public ResponseEntity<ApiResponse<KeyPointsResponse>> keyPoints(
            @Valid @RequestBody KeyPointsRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        KeyPointsResponse response = reviewerAIService.extractKeyPoints(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Gợi ý độ tương đồng reviewer-paper.
     * Chỉ dành cho Chair/Admin, dùng khi assign reviewer.
     */
    @PostMapping("/assignment/similarity-hint")
    @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SimilarityHintResponse>> similarityHint(
            @Valid @RequestBody SimilarityHintRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        SimilarityHintResponse response = reviewerAIService.calculateSimilarityHint(request, userId);
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
