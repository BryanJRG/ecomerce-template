package com.bjdev.ecomercebase.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Generic "resource not found / not yours" exception for domains outside auth (see AuthException for that one). */
@Getter
public class NotFoundException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final Object[] args;

    public NotFoundException(HttpStatus status, String errorCode, Object... args) {
        super(errorCode);
        this.status = status;
        this.errorCode = errorCode;
        this.args = args;
    }

    public static NotFoundException notification() {
        return new NotFoundException(HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND");
    }

    public static NotFoundException order() {
        return new NotFoundException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND");
    }

    public static NotFoundException shippingAddress() {
        return new NotFoundException(HttpStatus.NOT_FOUND, "SHIPPING_ADDRESS_NOT_FOUND");
    }
}
