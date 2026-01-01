package com.uth.confms.auth.service;

import com.uth.confms.auth.dto.*;
import com.uth.confms.auth.jwt.JwtUtil;
import com.uth.confms.user.entity.User;
import com.uth.confms.user.entity.Role;
import com.uth.confms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.getRoles().add(Role.AUTHOR);

        String refreshToken = jwtUtil.generateRefreshToken(user);
        user.setRefreshToken(refreshToken);

        userRepository.save(user);

        return new AuthResponse(
                jwtUtil.generateAccessToken(user),
                refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Sai email"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        String refreshToken = jwtUtil.generateRefreshToken(user);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new AuthResponse(
                jwtUtil.generateAccessToken(user),
                refreshToken);
    }

    public AuthResponse refreshToken(String refreshToken) {
        // 1. Tìm user theo refresh token
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ"));

        // 2. Tạo access token mới
        String newAccessToken = jwtUtil.generateAccessToken(user);

        // 3. (Tùy chọn) tạo refresh token mới và lưu lại
        String newRefreshToken = jwtUtil.generateRefreshToken(user);
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    public void logout(String refreshToken) {
        User user = userRepository.findAll().stream()
                .filter(u -> refreshToken.equals(u.getRefreshToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

        user.setRefreshToken(null);
        userRepository.save(user);
    }
}
