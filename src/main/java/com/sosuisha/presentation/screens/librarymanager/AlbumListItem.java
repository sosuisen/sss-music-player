package com.sosuisha.presentation.screens.librarymanager;

import java.util.Objects;

import com.sosuisha.domain.model.Album;
import com.sosuisha.domain.model.MusicFile;

/**
 * A row of the album list: an album, or a single track shown among the
 * favorites.
 */
public sealed interface AlbumListItem {
    /**
     * A row that shows an album.
     *
     * @param album album shown in the row
     */
    record AlbumItem(Album album) implements AlbumListItem {
        /**
         * Creates the row.
         *
         * @throws NullPointerException if album is null
         */
        public AlbumItem {
            Objects.requireNonNull(album, "album must not be null");
        }
    }

    /**
     * A row that shows a single track.
     *
     * @param track track shown in the row
     */
    record TrackItem(MusicFile track) implements AlbumListItem {
        /**
         * Creates the row.
         *
         * @throws NullPointerException if track is null
         */
        public TrackItem {
            Objects.requireNonNull(track, "track must not be null");
        }
    }
}
