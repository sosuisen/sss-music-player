package com.sosuisha.domain.exception;

/**
 * Thrown when the application settings cannot be saved or loaded.
 */
public class SettingsException extends RuntimeException {
    /**
     * Creates the exception.
     *
     * @param message description of the failure
     * @param cause underlying cause of the failure
     */
    public SettingsException(String message, Throwable cause) {
        super(message, cause);
    }
}
