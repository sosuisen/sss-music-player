package com.sosuisha.presentation.screens.alert;

import java.util.Objects;

import io.github.sosuisen.jfxbuilder.controls.AlertBuilder;

import javafx.scene.control.Alert.AlertType;

/**
 * Helper that shows common alert dialogs.
 */
public class AlertDialog {
    private AlertDialog() {}

    /**
     * Shows an error alert dialog with the given message. The dialog is shown
     * without blocking the caller.
     *
     * @param message message shown in the dialog
     * @throws NullPointerException if message is null
     */
    public static void showError(String message) {
        Objects.requireNonNull(message, "message must not be null");
        AlertBuilder.create(AlertType.ERROR)
            .contentText(message)
            .build()
            .show();
    }
}
