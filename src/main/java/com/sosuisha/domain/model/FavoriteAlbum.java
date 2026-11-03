package com.sosuisha.domain.model;

import java.util.Objects;

/**
 * An album registered as a favorite. The album is identified by its name and
 * its album artist, the same pair that groups the files into an album.
 *
 * @param name album name
 * @param artist album artist
 */
public record FavoriteAlbum(String name, String artist) implements Favorite {
    /**
     * Creates the favorite album.
     *
     * @throws NullPointerException if name or artist is null
     */
    public FavoriteAlbum {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(artist, "artist must not be null");
    }
}
