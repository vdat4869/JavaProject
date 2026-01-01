package com.uth.confms.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken) {
}
