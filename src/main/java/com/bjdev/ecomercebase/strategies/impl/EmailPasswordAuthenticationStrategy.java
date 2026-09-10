package com.bjdev.ecomercebase.strategies.impl;

import com.bjdev.ecomercebase.dto.request.LoginRequest;
import com.bjdev.ecomercebase.exception.AuthException;
import com.bjdev.ecomercebase.models.auth.AuthProvider;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.user.UserRepository;
import com.bjdev.ecomercebase.strategies.AuthenticationStrategy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EmailPasswordAuthenticationStrategy implements AuthenticationStrategy {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * A real BCrypt hash of a random value, computed once at startup. When the user isn't found
     * we still run a password comparison against this so the response takes the same time as a
     * wrong-password case — a failed lookup and a wrong password must be indistinguishable.
     */
    private String dummyHash;

    @PostConstruct
    void init() {
        this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.LOCAL;
    }

    @Override
    public User login(LoginRequest request, String clientIp) {
        if (request.email() == null || request.password() == null) {
            throw AuthException.invalidCredentials();
        }

        User user = userRepository.findByEmailWithRolesAndPermisos(request.email()).orElse(null);

        String hashToCheck = (user != null && user.getPasswordHash() != null) ? user.getPasswordHash() : dummyHash;
        boolean matches = passwordEncoder.matches(request.password(), hashToCheck);

        if (user == null || user.getPasswordHash() == null || !matches) {
            throw AuthException.invalidCredentials();
        }

        return user;
    }
}
