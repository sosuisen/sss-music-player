package com.sosuisha.repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import com.sosuisha.domain.model.Favorite;

/**
 * Favorites stored in the SQLite library database.
 */
public class SqliteFavoriteRepository {
    /**
     * Creates the repository on the given SQLite file.
     *
     * @param file path of the SQLite database file
     * @throws NullPointerException if file is null
     */
    public SqliteFavoriteRepository(Path file) {
        Objects.requireNonNull(file, "file must not be null");
    }

    /**
     * Returns all favorites in the order they were added.
     *
     * @return list of the favorites
     */
    public List<Favorite> findAll() {
        return List.of();
    }
}
