package com.uth.confms.auth.jwt;

import com.uth.confms.user.entity.User;
import com.uth.confms.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            UserRepository userRepository) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1️⃣ Lấy header Authorization
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            // 2️⃣ Validate token
            if (jwtProvider.validateToken(token)) {

                // 3️⃣ Lấy username từ token
                String username = jwtProvider.getUsernameFromToken(token);

                // 4️⃣ Load user từ DB
                User user = userRepository.findByUsername(username).orElse(null);

                if (user != null) {

                    // 5️⃣ Tạo Authentication
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            null,
                            null);

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // 6️⃣ Gắn vào SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        // 7️⃣ Cho request đi tiếp
        filterChain.doFilter(request, response);
    }
}
