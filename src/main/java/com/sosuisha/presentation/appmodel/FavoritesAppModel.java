package com.sosuisha.presentation.appmodel;

import java.util.Objects;

import com.sosuisha.domain.exception.RepositoryException;
import com.sosuisha.domain.model.Favorite;
import com.sosuisha.domain.repository.FavoriteRepository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Application-wide state of the favorites shared by the screens.
 */
public class FavoritesAppModel {
    private final ObservableList<Favorite> favorites = FXCollections.observableArrayList();
    private final FavoriteRepository repository;

    /**
     * Creates the app model and loads the favorites from the repository.
     *
     * @param repository repository that persists the favorites
     * @throws NullPointerException if repository is null
     * @throws RepositoryException if the favorites cannot be read
     */
    public FavoritesAppModel(FavoriteRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        favorites.setAll(repository.findAll());
    }

    /**
     * Returns the favorites in the order they were added.
     *
     * @return observable list of the favorites
     */
    public ObservableList<Favorite> getFavorites() {
        return favorites;
    }

    /**
     * Toggles the given favorite. An unregistered favorite is added at the
     * end of the favorites, and a registered one is removed. The change is
     * saved to the repository.
     *
     * @param favorite favorite to toggle
     * @throws NullPointerException if favorite is null
     * @throws RepositoryException if the change cannot be saved
     */
    public void toggle(Favorite favorite) {
        Objects.requireNonNull(favorite, "favorite must not be null");
        if (favorites.contains(favorite)) {
            repository.remove(favorite);
            favorites.remove(favorite);
        } else {
            repository.add(favorite);
            favorites.add(favorite);
        }
    }
}
