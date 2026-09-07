package com.bjdev.base.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public AuthException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public static AuthException invalidCredentials() {
        return new AuthException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Credenciales inválidas.");
    }

    public static AuthException accountLocked() {
        return new AuthException(HttpStatus.LOCKED, "ACCOUNT_LOCKED",
                "Cuenta bloqueada temporalmente por demasiados intentos fallidos.");
    }

    public static AuthException accountDisabled() {
        return new AuthException(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED", "Esta cuenta está deshabilitada.");
    }

    public static AuthException emailNotVerified() {
        return new AuthException(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED",
                "Debes verificar tu correo antes de iniciar sesión.");
    }

    public static AuthException emailAlreadyExists() {
        return new AuthException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS",
                "Ya existe una cuenta con ese correo.");
    }

    public static AuthException invalidToken() {
        return new AuthException(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "El token es inválido o ha expirado.");
    }

    public static AuthException invalidInvitation() {
        return new AuthException(HttpStatus.BAD_REQUEST, "INVALID_INVITATION",
                "La invitación es inválida, ya fue usada o ha expirado.");
    }

    public static AuthException invalidProvider() {
        return new AuthException(HttpStatus.BAD_REQUEST, "INVALID_PROVIDER",
                "Proveedor de autenticación no soportado para esta operación.");
    }

    public static AuthException userNotFound() {
        return new AuthException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado.");
    }

    public static AuthException invitationRequiresRegisteredUser() {
        return new AuthException(HttpStatus.BAD_REQUEST, "USER_NOT_REGISTERED",
                "El correo debe pertenecer a un usuario ya registrado en el sistema para poder invitarlo.");
    }

    public static AuthException invitationTargetNotVerified() {
        return new AuthException(HttpStatus.BAD_REQUEST, "USER_NOT_VERIFIED",
                "El usuario debe verificar su correo antes de poder ser invitado como administrador.");
    }

    public static AuthException invitationAlreadyPending() {
        return new AuthException(HttpStatus.CONFLICT, "INVITATION_ALREADY_PENDING",
                "Ya existe una invitación pendiente para este correo. Reenvíala o cancélala antes de crear una nueva.");
    }

    public static AuthException invitationNotPending() {
        return new AuthException(HttpStatus.CONFLICT, "INVITATION_NOT_PENDING",
                "Esta invitación ya fue resuelta y no puede modificarse.");
    }

    public static AuthException invitationEmailMismatch() {
        return new AuthException(HttpStatus.FORBIDDEN, "INVITATION_EMAIL_MISMATCH",
                "Esta invitación no corresponde a tu cuenta.");
    }

    public static AuthException userAlreadyAdmin() {
        return new AuthException(HttpStatus.CONFLICT, "USER_ALREADY_ADMIN", "Este usuario ya tiene rol de administrador.");
    }
}
