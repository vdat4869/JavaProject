package com.uth.confms.auth.repository;

import com.uth.confms.auth.entity.EmailVerificationToken;
import com.uth.confms.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationTokenRepository
    extends JpaRepository<EmailVerificationToken, Long> {
  Optional<EmailVerificationToken> findByToken(String token);

  Optional<EmailVerificationToken> findByUser(User user);

  void deleteByUser(User user);
}
