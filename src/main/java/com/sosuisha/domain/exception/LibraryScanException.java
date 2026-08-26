package com.sosuisha.domain.exception;

import org.jspecify.annotations.Nullable;

/**
 * Thrown when the music library folder or a file in it cannot be read during
 * a scan. The error is not recoverable by the caller, so this is an unchecked
 * exception. The message is written for the user and is required.
 */
public class LibraryScanException extends UnrecoverableException {
    /**
     * Creates the exception.
     *
     * @param message description of the failure, written for the user
     * @param cause underlying cause of the failure, or null when there is none
     * @throws NullPointerException if message is null
     * @throws IllegalArgumentException if message is blank
     */
    public LibraryScanException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
