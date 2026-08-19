package com.sosuisha.presentation.screens.settings;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.sosuisha.domain.model.Theme;
import com.sosuisha.presentation.appmodel.SettingsAppModel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.stage.Window;

/**
 * ViewModel for the settings screen. The settings state lives in the
 * {@link SettingsAppModel}; this view model delegates to it.
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
     * Returns the music library path property of the app model. It holds the
     * path of the music library folder, or null when no folder has been chosen
     * yet.
     *
     * @return object property of the music library path
     */
    public ObjectProperty<Path> musicLibraryPathProperty() {
        return appModel.musicLibraryPathProperty();
    }

    /**
     * Returns the theme property of the app model. It holds the current theme
     * of the application. The initial value is {@link Theme#PRIMER_LIGHT}.
     *
     * @return object property of the current theme
     */
    public ObjectProperty<Theme> themeProperty() {
        return appModel.themeProperty();
    }

    /**
     * Returns the music library path as an observable string shown in the
     * settings window.
     *
     * @return observable string of the music library path
     */
    public ObservableValue<String> musicLibraryPathTextProperty() {
        return appModel.musicLibraryPathProperty().map(Path::toString);
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
            .map(e -> "Failed to save the settings file: " + e.getMessage())
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
            .ifPresent(path -> {
                appModel.musicLibraryPathProperty().set(path);
                appModel.save();
            });
    }
}
