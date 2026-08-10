package com.sosuisha.presentation.screens.settings;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.sosuisha.domain.model.Settings;
import com.sosuisha.presentation.appmodel.SettingsAppModel;

import javafx.beans.value.ObservableValue;
import javafx.stage.Window;

/**
 * ViewModel for the settings screen.
 */
public class SettingsViewModel {
    private final SettingsAppModel appModel;
    private final Function<Window, Optional<Path>> directoryChooser;

    /**
     * Creates the view model.
     *
     * @param appModel application-wide state of the settings
     * @param directoryChooser function that lets the user choose a folder. It
     *            receives the owner window and returns the chosen folder, or an
     *            empty optional when the user cancels
     * @throws NullPointerException if appModel or directoryChooser is null
     */
    public SettingsViewModel(SettingsAppModel appModel,
        Function<Window, Optional<Path>> directoryChooser) {
        this.appModel = Objects.requireNonNull(appModel, "appModel must not be null");
        this.directoryChooser =
            Objects.requireNonNull(directoryChooser, "directoryChooser must not be null");
    }

    /**
     * Returns the music library path as an observable string. The value follows
     * the settings of the app model.
     *
     * @return observable string of the music library path
     */
    public ObservableValue<String> musicLibraryPathProperty() {
        return appModel.settingsProperty()
            .map(settings -> settings.musicLibraryPath().toString());
    }

    /**
     * Returns the error message as an observable string. The value follows the
     * error of the app model, and is an empty string when no error has
     * occurred.
     *
     * @return observable string of the error message
     */
    public ObservableValue<String> errorMessageProperty() {
        return appModel.errorProperty()
            .map(error -> "Failed to save the settings file: " + error.getMessage())
            .orElse("");
    }

    /**
     * Lets the user choose the music library folder and saves the chosen path
     * to the settings. Does nothing when the user cancels.
     *
     * @param ownerWindow window that owns the folder chooser dialog
     */
    public void selectMusicLibraryFolder(Window ownerWindow) {
        directoryChooser.apply(ownerWindow)
            .ifPresent(path -> appModel.saveSettings(new Settings(path)));
    }
}
