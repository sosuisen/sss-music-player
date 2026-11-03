package com.sosuisha.domain.repository;

import java.util.ArrayList;
import java.util.List;

import com.sosuisha.domain.model.Favorite;

/**
 * A favorite repository that keeps the favorites in memory. For tests that
 * need a {@link FavoriteRepository} without a database.
 */
public class InMemoryFavoriteRepository implements FavoriteRepository {
    private final List<Favorite> favorites;

    /**
     * Creates the repository holding no favorites.
     */
    public InMemoryFavoriteRepository() {
        this(List.of());
    }

    /**
     * Creates the repository holding the given favorites.
     *
     * @param favorites favorites in the order they were added
     */
    public InMemoryFavoriteRepository(List<Favorite> favorites) {
        this.favorites = new ArrayList<>(favorites);
    }

    @Override
    public List<Favorite> findAll() {
        return List.copyOf(favorites);
    }

    @Override
    public void add(Favorite favorite) {
        favorites.add(favorite);
    }

    @Override
    public void remove(Favorite favorite) {
        favorites.remove(favorite);
    }
}
