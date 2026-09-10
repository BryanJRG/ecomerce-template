package com.bjdev.ecomercebase.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Refund domain errors — see AuthException for the errorCode/args + messages.properties pattern. */
@Getter
public class RefundException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final Object[] args;

    private RefundException(HttpStatus status, String errorCode, Object... args) {
        super(errorCode);
        this.status = status;
        this.errorCode = errorCode;
        this.args = args;
    }

    public static RefundException refundNotFound() {
        return new RefundException(HttpStatus.NOT_FOUND, "REFUND_NOT_FOUND");
    }

    /** The order hasn't been paid (or was cancelled/already refunded) — nothing to refund. */
    public static RefundException orderNotRefundable() {
        return new RefundException(HttpStatus.CONFLICT, "ORDER_NOT_REFUNDABLE");
    }

    public static RefundException lineNotInOrder() {
        return new RefundException(HttpStatus.BAD_REQUEST, "REFUND_LINE_NOT_IN_ORDER");
    }

    public static RefundException quantityExceedsOrderLine() {
        return new RefundException(HttpStatus.BAD_REQUEST, "REFUND_QUANTITY_EXCEEDS_ORDER_LINE");
    }

    /** Same order line is already covered by a pending/approved/completed refund — never double-refund a unit. */
    public static RefundException lineAlreadyRefunded() {
        return new RefundException(HttpStatus.CONFLICT, "REFUND_LINE_ALREADY_REFUNDED");
    }

    public static RefundException invalidStatusTransition() {
        return new RefundException(HttpStatus.CONFLICT, "REFUND_INVALID_STATUS_TRANSITION");
    }
}
