package com.sosuisha.domain.service;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import com.sosuisha.domain.model.Settings;

/**
 * Persists the application settings.
 */
public interface SettingsRepository {
    /**
     * Saves the given settings.
     *
     * @param settings settings to save
     * @throws NullPointerException if settings is null
     * @throws IOException if the settings cannot be written
     */
    void save(Settings settings) throws IOException;

    /**
     * Loads the settings.
     *
     * @return loaded settings
     * @throws NoSuchFileException if there are no settings to load
     * @throws IOException if the settings cannot be read
     */
    Settings load() throws IOException;
}
