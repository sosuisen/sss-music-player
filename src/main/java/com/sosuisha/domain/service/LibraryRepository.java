package com.sosuisha.domain.service;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Optional;

import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.TrackMetadata;

/**
 * The library database of the app. Its first use is a cache of track metadata
 * for the library scan.
 */
public interface LibraryRepository {
    /**
     * Finds the cached track metadata of an audio file. An entry matches when
     * it has the same path and size and its last-modified time is at or after
     * the file's last-modified time.
     *
     * @param path path of the audio file
     * @param size size of the audio file in bytes
     * @param lastModified last-modified time of the audio file
     * @return the cached metadata, or an empty Optional if no entry matches
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

    /**
     * Returns the paths of all entries of the database.
     *
     * @return paths of all entries
     */
    List<Path> findAllPaths();

    /**
     * Deletes the entry of the given path. Does nothing when the path has no
     * entry.
     *
     * @param path path of the audio file whose entry is deleted
     */
    void delete(Path path);
}
