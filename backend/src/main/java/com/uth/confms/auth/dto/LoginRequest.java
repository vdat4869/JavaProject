package com.uth.confms.auth.dto;

public record LoginRequest(
        String username,
        String password) {
}
