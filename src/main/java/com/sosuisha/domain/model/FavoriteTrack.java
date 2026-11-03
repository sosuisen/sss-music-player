package com.sosuisha.domain.model;

import java.nio.file.Path;
import java.util.Objects;

/**
 * A track registered as a favorite. The track is identified by the path of
 * its audio file.
 *
 * @param path path of the audio file
 */
public record FavoriteTrack(Path path) implements Favorite {
    /**
     * Creates the favorite track.
     *
     * @throws NullPointerException if path is null
     */
    public FavoriteTrack {
        Objects.requireNonNull(path, "path must not be null");
    }
}
