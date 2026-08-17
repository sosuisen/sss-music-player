package com.sosuisha.presentation.screens.settings;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.sosuisha.domain.model.Settings;
import com.sosuisha.domain.model.Theme;
import com.sosuisha.domain.service.SettingsRepository;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.stage.Window;

/**
 * ViewModel for the settings screen. It owns the settings state (the music
 * library path and the theme) and persists it with the settings repository.
 * The {@link Settings} record is only assembled and taken apart at the
 * persistence boundary.
 */
public class SettingsViewModel {
    private final ObjectProperty<Path> musicLibraryPath = new SimpleObjectProperty<>();
    private final ObjectProperty<Theme> theme = new SimpleObjectProperty<>(Theme.PRIMER_LIGHT);
    private final ObjectProperty<Throwable> error = new SimpleObjectProperty<>();
    private final SettingsRepository repository;
    private final Function<Window, Optional<Path>> directoryChooser;

    /**
     * Creates the view model. When the theme changes, the settings are saved
     * with the new theme.
     *
     * @param repository repository that persists the settings
     * @param directoryChooser function that lets the user choose a folder. It
     *            receives the owner window and returns the chosen folder, or an
     *            empty optional when the user cancels
     * @throws NullPointerException if repository or directoryChooser is null
     */
    public SettingsViewModel(SettingsRepository repository,
        Function<Window, Optional<Path>> directoryChooser) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.directoryChooser =
            Objects.requireNonNull(directoryChooser, "directoryChooser must not be null");
        theme.subscribe((_, _) -> save());
    }

    /**
     * Returns the music library path property. It holds the path of the music
     * library folder, or null when no folder has been chosen yet.
     *
     * @return object property of the music library path
     */
    public ObjectProperty<Path> musicLibraryPathProperty() {
        return musicLibraryPath;
    }

    /**
     * Returns the theme property. It holds the current theme of the
     * application. The initial value is {@link Theme#PRIMER_LIGHT}.
     *
     * @return object property of the current theme
     */
    public ObjectProperty<Theme> themeProperty() {
        return theme;
    }

    /**
     * Returns the music library path as an observable string shown in the
     * settings window.
     *
     * @return observable string of the music library path
     */
    public ObservableValue<String> musicLibraryPathTextProperty() {
        return musicLibraryPath.map(Path::toString);
    }

    /**
     * Returns the error message as an observable string. It describes the
     * latest error of a settings save, and is an empty string when no error
     * has occurred.
     *
     * @return observable string of the error message
     */
    public ObservableValue<String> errorMessageProperty() {
        return error
            .map(e -> "Failed to save the settings file: " + e.getMessage())
            .orElse("");
    }

    /**
     * Loads the settings from the settings file and applies them to the
     * properties: the music library path and the theme follow the loaded
     * settings.
     *
     * @return loaded settings, or an empty optional when the settings file
     *         does not exist
     * @throws UncheckedIOException if the settings file exists but cannot be read
     */
    public Optional<Settings> loadSettings() {
        try {
            var loaded = repository.load();
            musicLibraryPath.set(loaded.musicLibraryPath());
            theme.set(loaded.theme());
            return Optional.of(loaded);
        } catch (NoSuchFileException e) {
            return Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
                musicLibraryPath.set(path);
                save();
            });
    }

    private void save() {
        var path = musicLibraryPath.get();
        if (path == null) { return; }
        try {
            repository.save(new Settings(path, theme.get()));
            error.set(null);
        } catch (IOException e) {
            error.set(e);
        }
    }
}
