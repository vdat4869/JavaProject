package com.uth.confms.auth.dto;

public record GoogleUserInfo(
        String sub,
        String email,
        String name,
        Boolean emailVerified) {
}
