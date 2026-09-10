package com.bjdev.ecomercebase.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class CheckoutException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final Object[] args;
    private final List<String> details;

    private CheckoutException(HttpStatus status, String errorCode, List<String> details, Object... args) {
        super(errorCode);
        this.status = status;
        this.errorCode = errorCode;
        this.details = details;
        this.args = args;
    }

    public static CheckoutException cartNotActive() {
        return new CheckoutException(HttpStatus.CONFLICT, "CART_NOT_ACTIVE", null);
    }

    public static CheckoutException emptyCart() {
        return new CheckoutException(HttpStatus.BAD_REQUEST, "EMPTY_CART", null);
    }

    /** Thrown BEFORE the payment provider is ever called — see CheckoutServiceImpl's reservation-first ordering. */
    public static CheckoutException outOfStock(String variantLabel) {
        return new CheckoutException(HttpStatus.CONFLICT, "OUT_OF_STOCK", null, variantLabel);
    }

    /**
     * {@code priceChangeDetails} carries the new cart's price-change notices (see
     * CloneCartResult) so a controller/frontend can show the customer what moved, without a
     * separate round-trip.
     */
    public static CheckoutException paymentDeclined(String reason, List<String> priceChangeDetails) {
        String effectiveReason = (reason != null && !reason.isBlank()) ? reason : "el proveedor de pago";
        return new CheckoutException(HttpStatus.PAYMENT_REQUIRED, "PAYMENT_DECLINED", priceChangeDetails, effectiveReason);
    }

    public static CheckoutException unsupportedPaymentType() {
        return new CheckoutException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_PAYMENT_TYPE", null);
    }

    /** The same idempotency key was reused for a request with a different amount/cart — never trust the cached result. */
    public static CheckoutException idempotencyKeyReused() {
        return new CheckoutException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED", null);
    }
}
