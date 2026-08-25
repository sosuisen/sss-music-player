package com.sosuisha.domain.exception;

/**
 * Thrown when the application settings cannot be saved or loaded.
 */
public class SettingsException extends UnrecoverableException {
    /**
     * Creates the exception.
     *
     * @param message description of the failure
     * @param cause underlying cause of the failure
     * @throws NullPointerException if message is null
     * @throws IllegalArgumentException if message is blank
     */
    public SettingsException(String message, Throwable cause) {
        super(message, cause);
    }
}
