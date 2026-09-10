package com.bjdev.ecomercebase.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Review domain errors — see AuthException for the errorCode/args + messages.properties pattern. */
@Getter
public class ReviewException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final Object[] args;

    private ReviewException(HttpStatus status, String errorCode, Object... args) {
        super(errorCode);
        this.status = status;
        this.errorCode = errorCode;
        this.args = args;
    }

    public static ReviewException reviewNotFound() {
        return new ReviewException(HttpStatus.NOT_FOUND, "REVIEW_NOT_FOUND");
    }

    /** Enforced at the service level (proactive check) even though uk_review_user_item would also catch it at the DB — gives a clean, specific error instead of a generic DATA_CONFLICT. */
    public static ReviewException alreadyReviewed() {
        return new ReviewException(HttpStatus.CONFLICT, "REVIEW_ALREADY_EXISTS");
    }
}
