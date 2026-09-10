package com.bjdev.ecomercebase.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Carries only a stable errorCode (+ optional MessageFormat args) — the actual user-facing text
 * lives in messages.properties, resolved by GlobalExceptionHandler via MessageSource. Keeping the
 * text out of the exception classes means every user-facing string lives in one file instead of
 * being scattered (and duplicated) across them.
 */
@Getter
public class AuthException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final Object[] args;

    public AuthException(HttpStatus status, String errorCode, Object... args) {
        super(errorCode);
        this.status = status;
        this.errorCode = errorCode;
        this.args = args;
    }

    public static AuthException invalidCredentials() {
        return new AuthException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }

    public static AuthException accountLocked() {
        return new AuthException(HttpStatus.LOCKED, "ACCOUNT_LOCKED");
    }

    public static AuthException accountDisabled() {
        return new AuthException(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED");
    }

    public static AuthException emailNotVerified() {
        return new AuthException(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED");
    }

    public static AuthException emailAlreadyExists() {
        return new AuthException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS");
    }

    public static AuthException invalidToken() {
        return new AuthException(HttpStatus.BAD_REQUEST, "INVALID_TOKEN");
    }

    public static AuthException invalidInvitation() {
        return new AuthException(HttpStatus.BAD_REQUEST, "INVALID_INVITATION");
    }

    public static AuthException invalidProvider() {
        return new AuthException(HttpStatus.BAD_REQUEST, "INVALID_PROVIDER");
    }

    public static AuthException userNotFound() {
        return new AuthException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }

    public static AuthException invitationRequiresRegisteredUser() {
        return new AuthException(HttpStatus.BAD_REQUEST, "USER_NOT_REGISTERED");
    }

    public static AuthException invitationTargetNotVerified() {
        return new AuthException(HttpStatus.BAD_REQUEST, "USER_NOT_VERIFIED");
    }

    public static AuthException invitationAlreadyPending() {
        return new AuthException(HttpStatus.CONFLICT, "INVITATION_ALREADY_PENDING");
    }

    public static AuthException invitationNotPending() {
        return new AuthException(HttpStatus.CONFLICT, "INVITATION_NOT_PENDING");
    }

    public static AuthException invitationEmailMismatch() {
        return new AuthException(HttpStatus.FORBIDDEN, "INVITATION_EMAIL_MISMATCH");
    }

    public static AuthException userAlreadyAdmin() {
        return new AuthException(HttpStatus.CONFLICT, "USER_ALREADY_ADMIN");
    }
}
