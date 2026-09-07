package com.bjdev.base.repositories.auth;

import com.bjdev.base.models.auth.EmailVerificationToken;
import com.bjdev.base.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    List<EmailVerificationToken> findAllByUserAndUsedAtIsNull(User user);
}
