package com.uth.confms.auth.controller;

import com.uth.confms.common.dto.ApiResponse;
import com.uth.confms.auth.dto.EmailVerificationRequest;
import com.uth.confms.email.service.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/email-verification")
public class EmailVerificationController {
  private final EmailVerificationService emailVerificationService;

  public EmailVerificationController(EmailVerificationService emailVerificationService) {
    this.emailVerificationService = emailVerificationService;
  }

  @PostMapping("/verify")
  public ResponseEntity<ApiResponse<Void>> verifyEmail(
      @Valid @RequestBody EmailVerificationRequest request) {
    emailVerificationService.verifyEmail(request.getToken());
    return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
  }

  @PostMapping("/resend")
  public ResponseEntity<ApiResponse<Void>> resendVerificationToken(@RequestParam String email) {
    emailVerificationService.resendVerificationToken(email);
    return ResponseEntity.ok(ApiResponse.success("Verification email sent", null));
  }
}
