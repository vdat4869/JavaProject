package com.uth.confms.auth.service;

import com.uth.confms.auth.dto.LoginRequest;
import com.uth.confms.auth.dto.TokenResponse;
import com.uth.confms.auth.jwt.JwtProvider;
import com.uth.confms.user.entity.User;
import com.uth.confms.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public TokenResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return new TokenResponse(
                jwtProvider.generateAccessToken(user),
                jwtProvider.generateRefreshToken(user));
    }
}
