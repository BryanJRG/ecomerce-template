package com.bjdev.ecomercebase.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

/** Discount domain errors — see AuthException for the errorCode/args + messages.properties pattern. */
@Getter
public class DiscountException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final Object[] args;

    private DiscountException(HttpStatus status, String errorCode, Object... args) {
        super(errorCode);
        this.status = status;
        this.errorCode = errorCode;
        this.args = args;
    }

    public static DiscountException discountNotFound() {
        return new DiscountException(HttpStatus.NOT_FOUND, "DISCOUNT_NOT_FOUND");
    }

    public static DiscountException codeAlreadyInUse(String code) {
        return new DiscountException(HttpStatus.CONFLICT, "DISCOUNT_CODE_ALREADY_IN_USE", code);
    }

    /** Exactly one of {applyToAllCatalog, categoryIds, itemIds, variantIds} must be selected. */
    public static DiscountException invalidTargeting() {
        return new DiscountException(HttpStatus.BAD_REQUEST, "DISCOUNT_INVALID_TARGETING");
    }

    /** excludedVariantIds cannot be combined with VARIANT targeting — inclusion is already explicit there. */
    public static DiscountException invalidExclusion() {
        return new DiscountException(HttpStatus.BAD_REQUEST, "DISCOUNT_INVALID_EXCLUSION");
    }

    public static DiscountException invalidDateRange() {
        return new DiscountException(HttpStatus.BAD_REQUEST, "DISCOUNT_INVALID_DATE_RANGE");
    }

    /** Code doesn't exist, isn't active, or is outside its date range — never distinguish which, to avoid leaking which codes almost worked. */
    public static DiscountException invalidCode() {
        return new DiscountException(HttpStatus.BAD_REQUEST, "DISCOUNT_CODE_INVALID");
    }

    public static DiscountException minPurchaseNotMet(BigDecimal required) {
        return new DiscountException(HttpStatus.BAD_REQUEST, "DISCOUNT_MIN_PURCHASE_NOT_MET", required.toPlainString());
    }

    public static DiscountException usageLimitReached() {
        return new DiscountException(HttpStatus.CONFLICT, "DISCOUNT_USAGE_LIMIT_REACHED");
    }

    /** The code is valid but nothing in the cart falls under its targeting (or its exclusions cover all of it). */
    public static DiscountException notApplicable() {
        return new DiscountException(HttpStatus.BAD_REQUEST, "DISCOUNT_NOT_APPLICABLE");
    }
}
