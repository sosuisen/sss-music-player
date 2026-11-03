package com.sosuisha.repository;

import static com.sosuisha.db.Tables.FAVORITE;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import com.sosuisha.db.tables.records.FavoriteRecord;
import com.sosuisha.domain.exception.RepositoryException;
import com.sosuisha.domain.model.Favorite;
import com.sosuisha.domain.model.FavoriteAlbum;
import com.sosuisha.domain.model.FavoriteTrack;

/**
 * Favorites stored in the SQLite library database.
 */
public class SqliteFavoriteRepository {
    private static final String ALBUM_KIND = "album";
    private static final String TRACK_KIND = "track";

    private final SqliteDatabase database;

    /**
     * Creates the repository on the given SQLite file. The file, its parent
     * folder, and the schema are created when they do not exist.
     *
     * @param file path of the SQLite database file
     * @throws NullPointerException if file is null
     * @throws RepositoryException if the parent folder cannot be created or the
     *             database cannot be opened
     */
    public SqliteFavoriteRepository(Path file) {
        Objects.requireNonNull(file, "file must not be null");
        this.database = new SqliteDatabase(file);
    }

    /**
     * Returns all favorites in the order they were added.
     *
     * @return list of the favorites
     * @throws RepositoryException if the database cannot be read
     */
    public List<Favorite> findAll() {
        return database.query(
            "Could not read the library database",
            dsl -> dsl.selectFrom(FAVORITE)
                .orderBy(FAVORITE.ID)
                .fetch(SqliteFavoriteRepository::toFavorite)
        );
    }

    private static Favorite toFavorite(FavoriteRecord record) {
        if (record.getKind().equals(ALBUM_KIND)) {
            return new FavoriteAlbum(record.getAlbumName(), record.getAlbumArtist());
        }
        return new FavoriteTrack(Path.of(record.getTrackPath()));
    }

    /**
     * Adds the given favorite after the existing ones.
     *
     * @param favorite favorite to add
     * @throws NullPointerException if favorite is null
     * @throws RepositoryException if the database cannot be written
     */
    public void add(Favorite favorite) {
        Objects.requireNonNull(favorite, "favorite must not be null");
        database.run("Could not write to the library database", dsl -> {
            var record = dsl.newRecord(FAVORITE);
            switch (favorite) {
                case FavoriteAlbum album -> {
                    record.setKind(ALBUM_KIND);
                    record.setAlbumName(album.name());
                    record.setAlbumArtist(album.artist());
                    record.setTrackPath("");
                }
                case FavoriteTrack track -> {
                    record.setKind(TRACK_KIND);
                    record.setAlbumName("");
                    record.setAlbumArtist("");
                    record.setTrackPath(track.path().toString());
                }
            }
            record.insert();
        });
    }

    /**
     * Removes the given favorite. Does nothing when it is not registered.
     *
     * @param favorite favorite to remove
     * @throws NullPointerException if favorite is null
     * @throws RepositoryException if the database cannot be written
     */
    public void remove(Favorite favorite) {
        Objects.requireNonNull(favorite, "favorite must not be null");
        database.run("Could not write to the library database", dsl -> {
            switch (favorite) {
                case FavoriteAlbum album -> dsl.deleteFrom(FAVORITE)
                    .where(
                        FAVORITE.KIND.eq(ALBUM_KIND)
                            .and(FAVORITE.ALBUM_NAME.eq(album.name()))
                            .and(FAVORITE.ALBUM_ARTIST.eq(album.artist()))
                    )
                    .execute();
                case FavoriteTrack track -> dsl.deleteFrom(FAVORITE)
                    .where(
                        FAVORITE.KIND.eq(TRACK_KIND)
                            .and(FAVORITE.TRACK_PATH.eq(track.path().toString()))
                    )
                    .execute();
            }
        });
    }
}
