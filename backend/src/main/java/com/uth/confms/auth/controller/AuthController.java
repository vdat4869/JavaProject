package com.uth.confms.auth.controller;

import com.uth.confms.auth.dto.LoginRequest;
import com.uth.confms.auth.dto.TokenResponse;
import com.uth.confms.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
