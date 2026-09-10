package com.bjdev.ecomercebase.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int           status,
        String        errorCode,
        String        error,
        String        message,
        String        path,
        List<String> details
) {

    // ─── Factory methods ──────────────────────────────────────────────────────

    /**
     * Creates an {@code ErrorResponse} without additional details.
     *
     * @param httpStatus HTTP status to return.
     * @param errorCode  Stable frontend key (e.g. {@code "RESOURCE_NOT_FOUND"}).
     * @param message    User-facing message describing the error.
     * @param path       Request path that triggered the error.
     */
    public static ErrorResponse of(HttpStatus httpStatus, String errorCode,
                                   String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                httpStatus.value(),
                errorCode,
                httpStatus.getReasonPhrase(),
                message,
                path,
                null
        );
    }

    /**
     * Creates an {@code ErrorResponse} with an optional list of detail messages
     * (e.g. per-field validation errors).
     *
     * @param httpStatus HTTP status to return.
     * @param errorCode  Stable frontend key.
     * @param message    User-facing message describing the error.
     * @param path       Request path that triggered the error.
     * @param details    List of detail messages; may be {@code null}.
     */
    public static ErrorResponse of(HttpStatus httpStatus, String errorCode,
                                   String message, String path, List<String> details) {
        return new ErrorResponse(
                LocalDateTime.now(),
                httpStatus.value(),
                errorCode,
                httpStatus.getReasonPhrase(),
                message,
                path,
                details
        );
    }
}