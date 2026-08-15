package com.sosuisha.domain.service;

import java.nio.file.Path;

/**
 * Writes track metadata to the tag of an audio file.
 */
public interface TagWriter {
    /**
     * Writes the album fields to the tag of an audio file. The other fields of
     * the tag are kept.
     *
     * @param file path of the audio file
     * @param album album name
     * @param albumArtist album artist
     * @param year release year
     */
    void writeAlbumTag(Path file, String album, String albumArtist, String year);
}
