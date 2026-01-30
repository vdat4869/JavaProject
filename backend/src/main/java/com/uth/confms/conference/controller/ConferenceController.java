package com.uth.confms.conference.controller;

import com.uth.confms.auth.service.UserService;
import com.uth.confms.common.annotations.NoAuth;
import com.uth.confms.common.dto.ApiResponse;
import com.uth.confms.conference.dto.ConferenceCreateDTO;
import com.uth.confms.conference.dto.ConferenceResponseDTO;
import com.uth.confms.conference.dto.ConferenceUpdateDTO;
import com.uth.confms.conference.service.ConferenceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller quản lý hội nghị (Conference)
 *
 * <p>
 * Các endpoints:
 *
 * <ul>
 * <li>GET /api/conferences/public - Lấy danh sách hội nghị đã publish (public)
 * <li>GET /api/conferences/{id} - Lấy thông tin hội nghị (public)
 * <li>GET /api/conferences/my - Lấy danh sách hội nghị của chair (CHAIR/ADMIN)
 * <li>POST /api/conferences - Tạo hội nghị mới (ADMIN only)
 * <li>PUT /api/conferences/{id} - Cập nhật hội nghị (CHAIR/ADMIN)
 * <li>DELETE /api/conferences/{id} - Xóa hội nghị (ADMIN only)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/conferences")
public class ConferenceController {
  private final ConferenceService conferenceService;
  private final UserService userService;

  public ConferenceController(ConferenceService conferenceService, UserService userService) {
    this.conferenceService = conferenceService;
    this.userService = userService;
  }

  @GetMapping("/public")
  @NoAuth
  // Lấy danh sách hội nghị đã được public
  public ResponseEntity<ApiResponse<List<ConferenceResponseDTO>>> getPublishedConferences() {
    return ResponseEntity.ok(ApiResponse.success(conferenceService.getPublishedConferences()));
  }

  @GetMapping("/{id}")
  @NoAuth
  // Lấy thông tin chi tiết hội nghị
  public ResponseEntity<ApiResponse<ConferenceResponseDTO>> getConference(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success(conferenceService.getConference(id)));
  }

  @GetMapping("/my")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // Lấy danh sách hội nghị của user (dành cho Chair/Admin)
  public ResponseEntity<ApiResponse<List<ConferenceResponseDTO>>> getMyConferences(
      Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(conferenceService.getConferencesByChair(chairId)));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  // Tạo hội nghị mới (Chỉ Admin)
  public ResponseEntity<ApiResponse<ConferenceResponseDTO>> createConference(
      @Valid @RequestBody ConferenceCreateDTO dto, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(conferenceService.createConference(dto, chairId)));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // Cập nhật thông tin hội nghị (Chair/Admin)
  public ResponseEntity<ApiResponse<ConferenceResponseDTO>> updateConference(
      @PathVariable Long id,
      @Valid @RequestBody ConferenceUpdateDTO dto,
      Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(conferenceService.updateConference(id, dto, chairId)));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  // Xóa hội nghị (Chỉ Admin)
  public ResponseEntity<ApiResponse<Void>> deleteConference(
      @PathVariable Long id, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    conferenceService.deleteConference(id, chairId);
    return ResponseEntity.ok(ApiResponse.success("Conference deleted successfully", null));
  }

  private Long getUserIdFromAuthentication(Authentication authentication) {
    String email = authentication.getName();
    return userService.getUserIdByEmail(email);
  }
}
