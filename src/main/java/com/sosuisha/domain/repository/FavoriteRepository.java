package com.sosuisha.domain.repository;

import java.util.List;

import com.sosuisha.domain.exception.RepositoryException;
import com.sosuisha.domain.model.Favorite;

/**
 * The favorites of the user, kept in the order they were added.
 */
public interface FavoriteRepository {
    /**
     * Returns all favorites in the order they were added.
     *
     * @return list of the favorites
     * @throws RepositoryException if the favorites cannot be read
     */
    List<Favorite> findAll() throws RepositoryException;

    /**
     * Adds the given favorite after the existing ones.
     *
     * @param favorite favorite to add
     * @throws NullPointerException if favorite is null
     * @throws RepositoryException if the favorite cannot be written
     */
    void add(Favorite favorite) throws RepositoryException;

    /**
     * Removes the given favorite. Does nothing when it is not registered.
     *
     * @param favorite favorite to remove
     * @throws NullPointerException if favorite is null
     * @throws RepositoryException if the favorite cannot be written
     */
    void remove(Favorite favorite) throws RepositoryException;
}
