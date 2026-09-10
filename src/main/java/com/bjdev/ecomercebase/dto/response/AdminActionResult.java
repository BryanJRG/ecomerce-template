package com.bjdev.ecomercebase.dto.response;

/**
 * Wraps the response of an admin action that may require email confirmation before it actually
 * runs (see AdminActionConfirmationService). {@code pending=true} means the action was NOT applied
 * yet — it takes effect only once the requester confirms via the emailed link.
 */
public record AdminActionResult<T>(boolean pending, String message, T data) {

    public static <T> AdminActionResult<T> pending(String message) {
        return new AdminActionResult<>(true, message, null);
    }

    public static <T> AdminActionResult<T> completed(T data) {
        return new AdminActionResult<>(false, null, data);
    }
}
