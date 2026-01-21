package com.uth.confms.auth.security;

import com.uth.confms.auth.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    // ⚠️ Demo – sau này đưa vào application.yml
    private static final String SECRET = "uth-confms-super-secret-key-uth-confms-2026";

    private static final long EXPIRATION = 24 * 60 * 60 * 1000; // 1 ngày

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(User user) {

        Set<String> roles = user.getRoles()
                .stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }
}
