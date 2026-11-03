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

    /**
     * Creates the app model and loads the favorites from the repository.
     *
     * @param repository repository that persists the favorites
     * @throws NullPointerException if repository is null
     * @throws RepositoryException if the favorites cannot be read
     */
    public FavoritesAppModel(FavoriteRepository repository) {
        Objects.requireNonNull(repository, "repository must not be null");
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
}
