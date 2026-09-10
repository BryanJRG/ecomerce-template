package com.bjdev.ecomercebase.exception;

import com.bjdev.ecomercebase.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

/**
 * Every user-facing error string lives in messages.properties, keyed by the errorCode each
 * exception already carries as a stable frontend key — resolved here via MessageSource instead
 * of being hardcoded (and duplicated) across exception classes. See AuthException's class doc.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    //Helper: path extraction
    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    /** Empty/null args skip MessageFormat entirely, so plain messages never need apostrophe-escaping. */
    private String resolve(String errorCode, Object... args) {
        Object[] effectiveArgs = (args != null && args.length > 0) ? args : null;
        return messageSource.getMessage(errorCode, effectiveArgs, errorCode, LocaleContextHolder.getLocale());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                resolve("VALIDATION_ERROR"),
                extractPath(request),
                details
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                ex.getStatus(), ex.getErrorCode(), resolve(ex.getErrorCode(), ex.getArgs()), extractPath(request));
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                ex.getStatus(), ex.getErrorCode(), resolve(ex.getErrorCode(), ex.getArgs()), extractPath(request));
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(CatalogException.class)
    public ResponseEntity<ErrorResponse> handleCatalogException(CatalogException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(ex.getStatus(), ex.getErrorCode(),
                resolve(ex.getErrorCode(), ex.getArgs()), extractPath(request), ex.getDetails());
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(CartException.class)
    public ResponseEntity<ErrorResponse> handleCartException(CartException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                ex.getStatus(), ex.getErrorCode(), resolve(ex.getErrorCode(), ex.getArgs()), extractPath(request));
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(RefundException.class)
    public ResponseEntity<ErrorResponse> handleRefundException(RefundException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                ex.getStatus(), ex.getErrorCode(), resolve(ex.getErrorCode(), ex.getArgs()), extractPath(request));
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(ReviewException.class)
    public ResponseEntity<ErrorResponse> handleReviewException(ReviewException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                ex.getStatus(), ex.getErrorCode(), resolve(ex.getErrorCode(), ex.getArgs()), extractPath(request));
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(DiscountException.class)
    public ResponseEntity<ErrorResponse> handleDiscountException(DiscountException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                ex.getStatus(), ex.getErrorCode(), resolve(ex.getErrorCode(), ex.getArgs()), extractPath(request));
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(CheckoutException.class)
    public ResponseEntity<ErrorResponse> handleCheckoutException(CheckoutException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(ex.getStatus(), ex.getErrorCode(),
                resolve(ex.getErrorCode(), ex.getArgs()), extractPath(request), ex.getDetails());
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(
            UsernameNotFoundException ex, WebRequest request){

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.NOT_FOUND,
                "USER_NOT_FOUND",
                resolve("USER_NOT_FOUND"),
                extractPath(request)
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", resolve("INVALID_CREDENTIALS"), extractPath(request));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED", resolve("ACCOUNT_DISABLED"), extractPath(request));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(LockedException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.LOCKED, "ACCOUNT_LOCKED", resolve("ACCOUNT_LOCKED"), extractPath(request));
        return ResponseEntity.status(HttpStatus.LOCKED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.FORBIDDEN, "ACCESS_DENIED", resolve("ACCESS_DENIED"), extractPath(request));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.CONFLICT, "DATA_CONFLICT", resolve("DATA_CONFLICT"), extractPath(request));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", resolve("INTERNAL_ERROR"), extractPath(request));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
