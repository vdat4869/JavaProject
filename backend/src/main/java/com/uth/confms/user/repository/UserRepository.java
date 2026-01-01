package com.uth.confms.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.uth.confms.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByRefreshToken(String refreshToken);
}
