package com.nortadas.web.error;

/**
 * Thrown by the web layer when the client supplies pagination parameters outside
 * the accepted range ({@code page < 0}, or {@code size} outside {@code 1..100}).
 * Translated to an RFC-7807 {@code 400 Bad Request} by {@link ApiExceptionHandler}.
 */
public class InvalidPaginationException extends RuntimeException {

    public InvalidPaginationException(String message) {
        super(message);
    }
}
