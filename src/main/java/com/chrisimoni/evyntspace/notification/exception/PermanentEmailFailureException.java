package com.chrisimoni.evyntspace.notification.exception;

/**
 * Exception thrown when email sending fails due to a permanent, non-retryable error.
 * Examples: invalid credentials, malformed MIME message, invalid email format.
 * These failures should be logged but NOT saved to the outbox for retry.
 */
public class PermanentEmailFailureException extends RuntimeException {

    public PermanentEmailFailureException(String message) {
        super(message);
    }

    public PermanentEmailFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
