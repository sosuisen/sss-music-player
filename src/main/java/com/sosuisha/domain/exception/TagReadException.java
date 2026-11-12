package com.sosuisha.domain.exception;

import java.util.Objects;

/**
 * Thrown after a library scan in which the tags of some files could not be
 * read because of an error in the application itself, for example a class that
 * could not be loaded. The scan has finished and those files have empty
 * metadata.
 * <p>
 * The cause is a bug, not an I/O error. So this is not an
 * {@link UnrecoverableException}: the stack trace of the cause must be shown to
 * the user for a bug report.
 */
public class TagReadException extends RuntimeException {
    /**
     * Creates the exception.
     *
     * @param message description of the failure, naming the files
     * @param cause error that stopped the first failed tag read
     * @throws NullPointerException if message or cause is null
     */
    public TagReadException(String message, Throwable cause) {
        super(
            Objects.requireNonNull(message, "message must not be null"),
            Objects.requireNonNull(cause, "cause must not be null")
        );
    }
}
