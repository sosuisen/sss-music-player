package com.sosuisha.domain.exception;

import java.util.Objects;

/**
 * Thrown when a repository cannot read or write its storage. The error is not
 * recoverable by the caller, so this is an unchecked exception. The message
 * is written for the user and is required.
 */
public class RepositoryException extends RuntimeException {
    /**
     * Creates the exception.
     *
     * @param message description of the failure, written for the user
     * @param cause underlying cause of the failure
     * @throws NullPointerException if message is null
     * @throws IllegalArgumentException if message is blank
     */
    public RepositoryException(String message, Throwable cause) {
        super(requireMessage(message), cause);
    }

    private static String requireMessage(String message) {
        Objects.requireNonNull(message, "message must not be null");
        if (message.isBlank()) { throw new IllegalArgumentException("message must not be blank"); }
        return message;
    }
}
