package com.sosuisha.domain.service;

import java.util.Optional;

import com.sosuisha.domain.exception.RepositoryException;
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
     * @throws RepositoryException if the settings cannot be written
     */
    void save(Settings settings) throws RepositoryException;

    /**
     * Loads the settings.
     *
     * @return loaded settings, or an empty optional when there are no settings
     *         to load
     * @throws RepositoryException if the settings cannot be read
     */
    Optional<Settings> load() throws RepositoryException;
}
