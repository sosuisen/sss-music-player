package com.sosuisha.domain.repository;

import com.sosuisha.domain.exception.RepositoryException;
import java.util.Optional;

import com.sosuisha.domain.model.Settings;

/**
 * A settings repository that saves nothing and has no settings file to load.
 * For tests that need a {@link SettingsRepository} but do not care about
 * persistence.
 */
public class NullSettingsRepository implements SettingsRepository {
    @Override
    public void save(Settings settings) throws RepositoryException {}

    @Override
    public Optional<Settings> load() throws RepositoryException {
        return Optional.empty();
    }
}
