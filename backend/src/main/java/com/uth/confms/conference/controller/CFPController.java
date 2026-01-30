package com.uth.confms.conference.controller;

import com.uth.confms.auth.service.UserService;
import com.uth.confms.common.annotations.NoAuth;
import com.uth.confms.common.dto.ApiResponse;
import com.uth.confms.conference.dto.CFPDTO;
import com.uth.confms.conference.dto.CFPResponseDTO;
import com.uth.confms.conference.service.CFPService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cfp")
public class CFPController {
  private final CFPService cfpService;
  private final UserService userService;

  public CFPController(CFPService cfpService, UserService userService) {
    this.cfpService = cfpService;
    this.userService = userService;
  }

  @GetMapping("/conference/{conferenceId}")
  @NoAuth
  // Lấy thông tin CFP của hội nghị
  public ResponseEntity<ApiResponse<CFPResponseDTO>> getCFP(@PathVariable Long conferenceId) {
    return ResponseEntity.ok(ApiResponse.success(cfpService.getCFPByConference(conferenceId)));
  }

  @PostMapping
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // Tạo hoặc cập nhật CFP
  public ResponseEntity<ApiResponse<CFPResponseDTO>> createOrUpdateCFP(
      @Valid @RequestBody CFPDTO dto, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(cfpService.createOrUpdateCFP(dto, chairId)));
  }

  @PostMapping("/{conferenceId}/publish")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // Mở CFP (Nhận bài nộp)
  public ResponseEntity<ApiResponse<CFPResponseDTO>> publishCFP(
      @PathVariable Long conferenceId, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(cfpService.publishCFP(conferenceId, chairId)));
  }

  @PostMapping("/{conferenceId}/close")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // Đóng CFP (Ngưng nhận bài nộp)
  public ResponseEntity<ApiResponse<CFPResponseDTO>> closeCFP(
      @PathVariable Long conferenceId, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(cfpService.closeCFP(conferenceId, chairId)));
  }

  private Long getUserIdFromAuthentication(Authentication authentication) {
    String email = authentication.getName();
    return userService.getUserIdByEmail(email);
  }
}
