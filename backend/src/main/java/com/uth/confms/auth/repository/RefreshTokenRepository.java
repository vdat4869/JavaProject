package com.uth.confms.auth.repository;

import com.uth.confms.auth.entity.RefreshToken;
import com.uth.confms.auth.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Dùng khi refresh access token
     */
    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

    /**
     * Logout 1 thiết bị
     */
    @Modifying
    @Transactional
    @Query("""
                update RefreshToken rt
                set rt.revoked = true
                where rt.tokenHash = :tokenHash
            """)
    void revokeByTokenHash(String tokenHash);

    /**
     * Logout tất cả thiết bị / đổi mật khẩu
     */
    @Modifying
    @Transactional
    @Query("""
                update RefreshToken rt
                set rt.revoked = true
                where rt.user = :user
            """)
    void revokeAllByUser(User user);

    /**
     * Cleanup token hết hạn (job định kỳ)
     */
    void deleteAllByExpiresAtBefore(LocalDateTime now);
}
