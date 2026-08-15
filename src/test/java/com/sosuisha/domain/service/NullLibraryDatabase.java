package com.sosuisha.domain.service;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Optional;

import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.TrackMetadata;

/**
 * A library database that never hits and saves nothing. For tests that need a
 * {@link LibraryDatabase} but do not care about caching.
 */
public class NullLibraryDatabase implements LibraryDatabase {
    @Override
    public Optional<TrackMetadata> find(Path path, long size, FileTime lastModified) {
        return Optional.empty();
    }

    @Override
    public void save(MusicFile file, FileTime lastModified) {}

    @Override
    public List<Path> findAllPaths() {
        return List.of();
    }

    @Override
    public void delete(Path path) {}
}
