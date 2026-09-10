package com.bjdev.ecomercebase.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CartException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final Object[] args;

    private CartException(HttpStatus status, String errorCode, Object... args) {
        super(errorCode);
        this.status = status;
        this.errorCode = errorCode;
        this.args = args;
    }

    public static CartException cartNotFound() {
        return new CartException(HttpStatus.NOT_FOUND, "CART_NOT_FOUND");
    }

    public static CartException variantInactive() {
        return new CartException(HttpStatus.CONFLICT, "VARIANT_INACTIVE");
    }

    public static CartException insufficientStock(String variantLabel, int available) {
        return new CartException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", variantLabel, available);
    }
}
