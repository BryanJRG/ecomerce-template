package com.bjdev.ecomercebase.repositories.auth;

import com.bjdev.ecomercebase.models.auth.EmailVerificationToken;
import com.bjdev.ecomercebase.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    List<EmailVerificationToken> findAllByUserAndUsedAtIsNull(User user);
}
