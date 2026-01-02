package com.uth.confms.auth.controller;

import com.uth.confms.auth.dto.*;
import com.uth.confms.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response) {

        AuthResponse auth = authService.login(request);

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", auth.getRefreshToken())
                .httpOnly(true)
                .secure(true) // true khi dùng HTTPS
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(
                new AuthResponse(auth.getAccessToken(), null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue("refreshToken") String refreshToken) {

        AuthResponse auth = authService.refresh(refreshToken);
        return ResponseEntity.ok(auth);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok().build();
    }

    // @PostMapping("/login")
    // public AuthResponse login(@RequestBody LoginRequest request) {
    // return authService.login(request);
    // }

    // @PostMapping("/refresh")
    // public ResponseEntity<AuthResponse> refreshToken(@RequestBody
    // RefreshTokenRequest request) {
    // AuthResponse response = authService.refreshToken(request.getRefreshToken());
    // return ResponseEntity.ok(response);
    // }

    // @PostMapping("/logout")
    // public void logout(@RequestParam String refreshToken) {
    // authService.logout(refreshToken);
    // }
}
