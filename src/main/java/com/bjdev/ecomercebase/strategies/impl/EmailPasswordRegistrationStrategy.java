package com.bjdev.ecomercebase.strategies.impl;

import com.bjdev.ecomercebase.dto.request.RegisterRequest;
import com.bjdev.ecomercebase.exception.AuthException;
import com.bjdev.ecomercebase.models.auth.AuthProvider;
import com.bjdev.ecomercebase.models.auth.LoginMethod;
import com.bjdev.ecomercebase.models.user.Role;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.auth.LoginMethodRepository;
import com.bjdev.ecomercebase.repositories.user.RoleRepository;
import com.bjdev.ecomercebase.repositories.user.UserRepository;
import com.bjdev.ecomercebase.strategies.RegistrationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EmailPasswordRegistrationStrategy implements RegistrationStrategy {

    private final UserRepository userRepository;
    private final LoginMethodRepository loginMethodRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.LOCAL;
    }

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw AuthException.invalidCredentials();
        }

        if (userRepository.findByEmailWithRolesAndPermisos(request.email()).isPresent()) {
            throw AuthException.emailAlreadyExists();
        }

        // The role is always the default USER role — a client can never self-assign a role.
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Default USER role is missing"));

        User user = User.builder()
                .email(request.email())
                .userName(request.userName())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .isActive(true)
                .isVerified(false)
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        user = userRepository.save(user);

        loginMethodRepository.save(LoginMethod.builder()
                .user(user)
                .provider(AuthProvider.LOCAL)
                .providerUserId(null)
                .createdAt(LocalDateTime.now())
                .build());

        return user;
    }
}
