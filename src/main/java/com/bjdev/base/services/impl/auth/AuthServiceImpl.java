package com.bjdev.base.services.impl.auth;

import com.bjdev.base.dto.request.ChangePasswordRequest;
import com.bjdev.base.dto.request.ForgotPasswordRequest;
import com.bjdev.base.dto.request.LoginRequest;
import com.bjdev.base.dto.request.RegisterRequest;
import com.bjdev.base.dto.request.ResendVerificationEmailRequest;
import com.bjdev.base.dto.request.ResetPasswordRequest;
import com.bjdev.base.dto.response.MessageResponse;
import com.bjdev.base.dto.response.UserResponse;
import com.bjdev.base.exception.AuthException;
import com.bjdev.base.mappers.UserMapper;
import com.bjdev.base.models.auth.AuthProvider;
import com.bjdev.base.models.auth.EmailVerificationToken;
import com.bjdev.base.models.auth.PasswordResetToken;
import com.bjdev.base.models.user.User;
import com.bjdev.base.repositories.auth.EmailVerificationTokenRepository;
import com.bjdev.base.repositories.auth.PasswordResetTokenRepository;
import com.bjdev.base.repositories.user.UserRepository;
import com.bjdev.base.security.jwt.JwtTokenProvider;
import com.bjdev.base.security.jwt.LoginAttemptService;
import com.bjdev.base.security.jwt.TokenBlackListService;
import com.bjdev.base.services.impl.audit.AuditService;
import com.bjdev.base.services.interfaces.AuthService;
import com.bjdev.base.services.interfaces.EmailService;
import com.bjdev.base.strategies.AuthStrategyResolver;
import com.bjdev.base.utils.CookieUtil;
import com.bjdev.base.utils.HttpRequestUtils;
import com.bjdev.base.utils.TokenHasher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlackListService tokenBlackListService;
    private final LoginAttemptService loginAttemptService;

    private final AuthStrategyResolver authStrategyResolver;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final AuditService auditService;
    private final CookieUtil cookieUtil;
    private final PasswordEncoder passwordEncoder;

    /** When enabled, every login invalidates that user's previously issued tokens (single session per user). */
    @Value("${app.security.single-session-enabled:false}")
    private boolean singleSessionEnabled;

    @Override
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        User user = authStrategyResolver.resolveRegister(request.provider()).register(request);

        String rawToken = generateRandomToken();
        emailVerificationTokenRepository.save(EmailVerificationToken.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build());

        emailService.sendVerificationEmail(user.getEmail(), rawToken);
        auditService.record(user, "REGISTER", "users", user.getId(), null, null, null);

        return new MessageResponse("Registro exitoso. Revisa tu correo para verificar tu cuenta.");
    }

    @Override
    @Transactional
    public UserResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String ip = HttpRequestUtils.getClientIp(httpRequest);
        String identifier = request.email() != null ? request.email() : "unknown";

        if (loginAttemptService.isBlocked(identifier, ip)) {
            throw AuthException.accountLocked();
        }

        User user;
        try {
            user = authStrategyResolver.resolveLogin(request.provider()).login(request, ip);
        } catch (AuthException ex) {
            if (request.provider() == AuthProvider.LOCAL) {
                loginAttemptService.registerFailure(identifier, ip);
            }
            auditService.record(null, "LOGIN_FAILED", "users", null, null, null, httpRequest);
            throw ex;
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw AuthException.accountDisabled();
        }
        if (request.provider() == AuthProvider.LOCAL && !Boolean.TRUE.equals(user.getIsVerified())) {
            throw AuthException.emailNotVerified();
        }

        loginAttemptService.registerSuccess(identifier, ip);

        // Enforces a single active session per user by invalidating every token issued in previous
        // logins (other devices included) before this login's tokens are handed out.
        if (singleSessionEnabled) {
            tokenBlackListService.invalidateAllUserTokens(user.getEmail());
        }
        issueTokens(user, httpResponse);
        auditService.record(user, "LOGIN", "users", user.getId(), null, null, httpRequest);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String refreshToken = extractCookie(httpRequest, CookieUtil.REFRESH_TOKEN_COOKIE);
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)
                || !"REFRESH".equals(jwtTokenProvider.getTokenType(refreshToken))) {
            throw AuthException.invalidToken();
        }

        String jti = jwtTokenProvider.getJtiFromToken(refreshToken);
        if (jti == null || tokenBlackListService.isTokenBlacklisted(jti)) {
            throw AuthException.invalidToken();
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        long issuedAt = jwtTokenProvider.getIssuedAtMillis(refreshToken);
        if (!tokenBlackListService.isTokenValidForUser(username, issuedAt)) {
            throw AuthException.invalidToken();
        }

        User user = userRepository.findByEmailWithRolesAndPermisos(username)
                .orElseThrow(AuthException::invalidToken);

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw AuthException.accountDisabled();
        }

        // Rotate the refresh token: blacklist the one just used before issuing a new pair.
        long remainingTtl = jwtTokenProvider.getRemainingTtlSeconds(refreshToken);
        tokenBlackListService.blacklistToken(jti, remainingTtl);

        issueTokens(user, httpResponse);
    }

    @Override
    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        blacklistCookie(httpRequest, CookieUtil.ACCESS_TOKEN_COOKIE);
        blacklistCookie(httpRequest, CookieUtil.REFRESH_TOKEN_COOKIE);
        cookieUtil.clearAuthCookies(httpResponse);
        auditService.record(getAuthenticatedUserOrNull(), "LOGOUT", "users", null, null, null, httpRequest);
    }

    @Override
    @Transactional
    public MessageResponse verifyEmail(String token) {
        EmailVerificationToken evt = emailVerificationTokenRepository.findByTokenHash(hashToken(token))
                .orElseThrow(AuthException::invalidToken);

        if (evt.getUsedAt() != null || evt.getExpiresAt().isBefore(Instant.now())) {
            throw AuthException.invalidToken();
        }

        User user = evt.getUser();
        user.setIsVerified(true);
        userRepository.save(user);

        evt.setUsedAt(Instant.now());
        emailVerificationTokenRepository.save(evt);

        auditService.record(user, "EMAIL_VERIFIED", "users", user.getId(), null, null, null);
        return new MessageResponse("Correo verificado correctamente.");
    }

    @Override
    @Transactional
    public MessageResponse resendVerificationEmail(ResendVerificationEmailRequest request) {
        userRepository.findByEmailWithRolesAndPermisos(request.email())
                .filter(user -> !Boolean.TRUE.equals(user.getIsVerified()))
                .ifPresent(user -> {
                    // Invalidate any still-usable previous tokens so only the newest link works.
                    emailVerificationTokenRepository.findAllByUserAndUsedAtIsNull(user).forEach(token -> {
                        token.setUsedAt(Instant.now());
                        emailVerificationTokenRepository.save(token);
                    });

                    String rawToken = generateRandomToken();
                    emailVerificationTokenRepository.save(EmailVerificationToken.builder()
                            .user(user)
                            .tokenHash(hashToken(rawToken))
                            .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                            .build());

                    emailService.sendVerificationEmail(user.getEmail(), rawToken);
                    auditService.record(user, "EMAIL_VERIFICATION_RESENT", "users", user.getId(), null, null, null);
                });

        // Same response whether or not the email exists or is already verified — no account enumeration.
        return new MessageResponse("Si el correo existe y no ha sido verificado, recibirás un nuevo enlace de verificación.");
    }

    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailWithRolesAndPermisos(request.email()).ifPresent(user -> {
            String rawToken = generateRandomToken();
            passwordResetTokenRepository.save(PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(hashToken(rawToken))
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .build());
            emailService.sendPasswordResetEmail(user.getEmail(), rawToken);
        });

        // Same response whether or not the email exists — no account enumeration.
        return new MessageResponse("Si el correo existe, recibirás instrucciones para restablecer tu contraseña.");
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hashToken(request.token()))
                .orElseThrow(AuthException::invalidToken);

        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw AuthException.invalidToken();
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(token);

        tokenBlackListService.invalidateAllUserTokens(user.getEmail());
        auditService.record(user, "PASSWORD_RESET", "users", user.getId(), null, null, null);

        return new MessageResponse("Contraseña actualizada correctamente. Inicia sesión nuevamente.");
    }

    @Override
    @Transactional
    public MessageResponse changePassword(ChangePasswordRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        User user = getAuthenticatedUser();

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw AuthException.invalidCredentials();
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        tokenBlackListService.invalidateAllUserTokens(user.getEmail());
        auditService.record(user, "PASSWORD_CHANGE", "users", user.getId(), null, null, httpRequest);

        // Every other session is now invalid; re-issue fresh tokens so this one keeps working.
        issueTokens(user, httpResponse);

        return new MessageResponse("Contraseña actualizada correctamente.");
    }

    @Override
    public UserResponse getCurrentUser() {
        return userMapper.toResponse(getAuthenticatedUser());
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private void issueTokens(User user, HttpServletResponse response) {
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getName()));
        String username = user.getEmail();

        String accessToken = isAdmin ? jwtTokenProvider.generateAdminToken(username) : jwtTokenProvider.generateToken(username);
        String refreshToken = isAdmin ? jwtTokenProvider.generateAdminRefreshToken(username) : jwtTokenProvider.generateRefreshToken(username);

        cookieUtil.addAccessTokenCookie(response, accessToken, jwtTokenProvider.getRemainingTtlSeconds(accessToken));
        cookieUtil.addRefreshTokenCookie(response, refreshToken, jwtTokenProvider.getRemainingTtlSeconds(refreshToken));
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }

    private void blacklistCookie(HttpServletRequest request, String cookieName) {
        String token = extractCookie(request, cookieName);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String jti = jwtTokenProvider.getJtiFromToken(token);
            long ttl = jwtTokenProvider.getRemainingTtlSeconds(token);
            tokenBlackListService.blacklistToken(jti, ttl);
        }
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailWithRolesAndPermisos(email)
                .orElseThrow(AuthException::invalidCredentials);
    }

    private User getAuthenticatedUserOrNull() {
        try {
            return getAuthenticatedUser();
        } catch (Exception e) {
            return null;
        }
    }

    private String generateRandomToken() {
        return TokenHasher.generateRandomToken();
    }

    private String hashToken(String rawToken) {
        return TokenHasher.hashToken(rawToken);
    }
}
