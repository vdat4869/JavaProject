package com.uth.confms.ai.controller;

import com.uth.confms.ai.dto.*;
import com.uth.confms.ai.service.AuthorAIService;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho các tính năng AI dành cho Author.
 *
 * <p>
 * Các endpoints:
 * <ul>
 * <li>POST /ai/author/spell-check - Kiểm tra chính tả
 * <li>POST /ai/author/abstract-polish - Cải thiện abstract
 * <li>POST /ai/author/keyword-suggest - Gợi ý keywords
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/ai/author")
public class AuthorAIController {

    private final AuthorAIService authorAIService;
    private final UserRepository userRepository;

    public AuthorAIController(AuthorAIService authorAIService, UserRepository userRepository) {
        this.authorAIService = authorAIService;
        this.userRepository = userRepository;
    }

    /**
     * Endpoint kiểm tra chính tả và ngữ pháp.
     */
    @PostMapping("/spell-check")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<ApiResponse<SpellCheckResponse>> spellCheck(
            @Valid @RequestBody SpellCheckRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        SpellCheckResponse response = authorAIService.spellCheck(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Cải thiện abstract với gợi ý từ AI.
     */
    @PostMapping("/abstract-polish")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<ApiResponse<AbstractPolishResponse>> polishAbstract(
            @Valid @RequestBody AbstractPolishRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        AbstractPolishResponse response = authorAIService.polishAbstract(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Gợi ý keywords dựa trên nội dung bài báo.
     */
    @PostMapping("/keyword-suggest")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<ApiResponse<KeywordSuggestResponse>> suggestKeywords(
            @Valid @RequestBody KeywordSuggestRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        KeywordSuggestResponse response = authorAIService.suggestKeywords(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lấy ID người dùng từ thông tin xác thực Spring Security.
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
