package com.sosuisha.domain.exception;

import java.util.Objects;

/**
 * Thrown when the music library folder or a file in it cannot be read during
 * a scan. The error is not recoverable by the caller, so this is an unchecked
 * exception. The message is written for the user and is required.
 */
public class LibraryScanException extends RuntimeException {
    /**
     * Creates the exception.
     *
     * @param message description of the failure, written for the user
     * @param cause underlying cause of the failure
     * @throws NullPointerException if message is null
     * @throws IllegalArgumentException if message is blank
     */
    public LibraryScanException(String message, Throwable cause) {
        super(requireMessage(message), cause);
    }

    private static String requireMessage(String message) {
        Objects.requireNonNull(message, "message must not be null");
        if (message.isBlank()) { throw new IllegalArgumentException("message must not be blank"); }
        return message;
    }
}
