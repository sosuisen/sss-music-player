package com.sosuisha.domain.service;

import java.nio.file.Path;

import com.sosuisha.domain.exception.TagWriteException;

/**
 * Writes track metadata to the tag of an audio file.
 */
public interface TagWriter {
    /**
     * Writes the album fields to the tag of an audio file. The other fields of
     * the tag, including the year, are kept.
     *
     * @param file path of the audio file
     * @param album album name
     * @param albumArtist album artist
     * @throws TagWriteException if the tag cannot be written
     */
    void writeAlbumTag(Path file, String album, String albumArtist) throws TagWriteException;
}
