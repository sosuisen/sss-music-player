package com.sosuisha.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * An album recognized in the music library.
 *
 * @param name album name
 * @param artist album artist
 * @param files audio files of the album
 */
public record Album(String name, String artist, List<MusicFile> files) {
    /**
     * Creates the album.
     *
     * @throws NullPointerException if name, artist, or files is null
     */
    public Album {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(artist, "artist must not be null");
        Objects.requireNonNull(files, "files must not be null");
    }
}
