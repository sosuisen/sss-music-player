package com.sosuisha.domain.model;

/**
 * An album or a track that the user registered as a favorite.
 */
public sealed interface Favorite permits FavoriteAlbum, FavoriteTrack {
}
