package com.sosuisha.domain.exception;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * Thrown when the tag of an audio file cannot be written. The caller is
 * expected to report the failure to the user and continue, so this is a
 * checked exception. The message is written for the user and is required.
 */
public class TagWriteException extends Exception {
    /**
     * Creates the exception.
     *
     * @param message description of the failure, written for the user
     * @param cause underlying cause of the failure, or null when there is none
     * @throws NullPointerException if message is null
     * @throws IllegalArgumentException if message is blank
     */
    public TagWriteException(String message, @Nullable Throwable cause) {
        super(requireMessage(message), cause);
    }

    private static String requireMessage(String message) {
        Objects.requireNonNull(message, "message must not be null");
        if (message.isBlank()) { throw new IllegalArgumentException("message must not be blank"); }
        return message;
    }
}
