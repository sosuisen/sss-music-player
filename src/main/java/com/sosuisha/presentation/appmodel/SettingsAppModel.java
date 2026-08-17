package com.sosuisha.presentation.appmodel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import com.sosuisha.domain.model.Settings;
import com.sosuisha.domain.model.Theme;
import com.sosuisha.domain.service.SettingsRepository;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Application-wide state of the settings shared by multiple screens. The
 * {@link Settings} record is only assembled and taken apart at the
 * persistence boundary; the screens observe the individual properties.
 */
public class SettingsAppModel {
    private final ObjectProperty<Path> musicLibraryPath = new SimpleObjectProperty<>();
    private final ObjectProperty<Theme> theme = new SimpleObjectProperty<>(Theme.PRIMER_LIGHT);
    private final ObjectProperty<Throwable> error = new SimpleObjectProperty<>();
    private final SettingsRepository repository;

    /**
     * Creates the app model.
     *
     * @param repository repository that persists the settings
     * @throws NullPointerException if repository is null
     */
    public SettingsAppModel(SettingsRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
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
     * Loads the settings from the settings file and applies them to this app
     * model: the music library path and the theme follow the loaded settings.
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
     * Saves the current settings (the music library path and the theme) to the
     * settings file. Does nothing when the music library path is not set. When
     * the settings file cannot be written, the error is set to the error
     * property; when it is saved, the error property is cleared.
     */
    public void save() {
        var path = musicLibraryPath.get();
        if (path == null) { return; }
        try {
            repository.save(new Settings(path, theme.get()));
            error.set(null);
        } catch (IOException e) {
            error.set(e);
        }
    }

    /**
     * Returns the error property. It holds the latest error of a settings
     * service call, or null when no error has occurred.
     *
     * @return object property of the error
     */
    public ObjectProperty<Throwable> errorProperty() {
        return error;
    }
}
