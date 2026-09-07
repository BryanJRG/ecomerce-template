package com.bjdev.base.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Generic "resource not found / not yours" exception for domains outside auth (see AuthException for that one). */
@Getter
public class NotFoundException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public NotFoundException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public static NotFoundException notification() {
        return new NotFoundException(HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND", "Notificación no encontrada.");
    }
}
