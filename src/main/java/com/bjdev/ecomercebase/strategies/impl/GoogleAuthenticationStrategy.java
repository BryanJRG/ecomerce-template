package com.bjdev.ecomercebase.strategies.impl;

import com.bjdev.ecomercebase.dto.request.LoginRequest;
import com.bjdev.ecomercebase.exception.AuthException;
import com.bjdev.ecomercebase.models.auth.AuthProvider;
import com.bjdev.ecomercebase.models.auth.LoginMethod;
import com.bjdev.ecomercebase.models.user.Role;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.auth.LoginMethodRepository;
import com.bjdev.ecomercebase.repositories.user.RoleRepository;
import com.bjdev.ecomercebase.repositories.user.UserRepository;
import com.bjdev.ecomercebase.security.GoogleTokenVerifier;
import com.bjdev.ecomercebase.strategies.AuthenticationStrategy;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Google "Sign in with Google" auto-provisions the account on first login (Google already
 * verified the email) and links the Google login method to an existing LOCAL account that
 * shares the same email — standard OAuth account-linking behavior.
 */
@Component
@RequiredArgsConstructor
public class GoogleAuthenticationStrategy implements AuthenticationStrategy {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserRepository userRepository;
    private final LoginMethodRepository loginMethodRepository;
    private final RoleRepository roleRepository;

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.GOOGLE;
    }

    @Override
    @Transactional
    public User login(LoginRequest request, String clientIp) {
        if (request.idToken() == null) {
            throw AuthException.invalidCredentials();
        }

        GoogleIdToken.Payload payload = googleTokenVerifier.verify(request.idToken());
        String email = payload.getEmail();
        String providerUserId = payload.getSubject();

        User user = userRepository.findByEmailWithRolesAndPermisos(email).orElse(null);

        if (user == null) {
            user = createUser(email, providerUserId, payload);
        } else if (!loginMethodRepository.existsByUserAndProvider(user, AuthProvider.GOOGLE)) {
            linkGoogleMethod(user, providerUserId);
            if (!Boolean.TRUE.equals(user.getIsVerified())) {
                // Google just proved ownership of this email address — but any LOCAL password
                // already set on this still-unverified account could have been chosen by someone
                // else entirely (e.g. an attacker pre-registers the victim's email with a password
                // only they know, hoping the real owner later "claims" it by signing in with Google).
                // Clear it so that password can no longer be used to log in; the real owner can set
                // a fresh one via forgot-password if they want LOCAL login too.
                user.setIsVerified(true);
                user.setPasswordHash(null);
                userRepository.save(user);
            }
        }

        return user;
    }

    private User createUser(String email, String providerUserId, GoogleIdToken.Payload payload) {
        String givenName = (String) payload.get("given_name");
        String familyName = (String) payload.get("family_name");

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Default USER role is missing"));

        User user = User.builder()
                .email(email)
                .userName(generateUserName(email))
                .firstName(givenName != null ? givenName : "Google")
                .lastName(familyName != null ? familyName : "User")
                .passwordHash(null)
                .isActive(true)
                .isVerified(true)
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        user = userRepository.save(user);
        linkGoogleMethod(user, providerUserId);
        return user;
    }

    private void linkGoogleMethod(User user, String providerUserId) {
        loginMethodRepository.save(LoginMethod.builder()
                .user(user)
                .provider(AuthProvider.GOOGLE)
                .providerUserId(providerUserId)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private String generateUserName(String email) {
        String base = email.split("@")[0];
        if (base.length() > 35) {
            base = base.substring(0, 35);
        }
        return base + "-" + Integer.toHexString((int) (Math.random() * 0xFFFF));
    }
}
