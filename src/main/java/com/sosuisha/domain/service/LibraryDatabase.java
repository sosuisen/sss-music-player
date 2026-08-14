package com.sosuisha.domain.service;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Optional;

import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.TrackMetadata;

/**
 * The library database of the app. Its first use is a cache of track metadata
 * for the library scan.
 */
public interface LibraryDatabase {
    /**
     * Finds the cached track metadata of an audio file.
     *
     * @param path path of the audio file
     * @param size size of the audio file in bytes
     * @param lastModified last-modified time of the audio file
     * @return the cached metadata, or an empty Optional if the file is not
     *     cached
     */
    Optional<TrackMetadata> find(Path path, long size, FileTime lastModified);

    /**
     * Saves the track metadata of an audio file to the database. An existing
     * entry of the same path is overwritten.
     *
     * @param file audio file whose path, size, and metadata are saved
     * @param lastModified last-modified time of the audio file
     */
    void save(MusicFile file, FileTime lastModified);
}
