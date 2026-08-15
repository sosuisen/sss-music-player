package com.sosuisha.domain.service;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import com.sosuisha.domain.model.Settings;

/**
 * A settings repository that saves nothing and has no settings file to load.
 * For tests that need a {@link SettingsRepository} but do not care about
 * persistence.
 */
public class NullSettingsRepository implements SettingsRepository {
    @Override
    public void save(Settings settings) throws IOException {}

    @Override
    public Settings load() throws IOException {
        throw new NoSuchFileException("NullSettingsRepository has no settings file");
    }
}
