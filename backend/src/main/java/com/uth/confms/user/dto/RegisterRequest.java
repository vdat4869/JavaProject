package com.uth.confms.user.dto;

public record RegisterRequest(
        String username,
        String password,
        String email) {
}
