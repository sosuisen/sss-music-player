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
import com.sosuisha.domain.repository.FavoriteRepository;

/**
 * Favorites stored in the SQLite library database.
 */
public class SqliteFavoriteRepository implements FavoriteRepository {
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
    @Override
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
    @Override
    public void add(Favorite favorite) {
        Objects.requireNonNull(favorite, "favorite must not be null");
        var row = Row.of(favorite);
        database.run("Could not write to the library database", dsl -> {
            var record = dsl.newRecord(FAVORITE);
            record.setKind(row.kind());
            record.setAlbumName(row.albumName());
            record.setAlbumArtist(row.albumArtist());
            record.setTrackPath(row.trackPath());
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
    @Override
    public void remove(Favorite favorite) {
        Objects.requireNonNull(favorite, "favorite must not be null");
        var row = Row.of(favorite);
        database.run(
            "Could not write to the library database", dsl -> dsl.deleteFrom(FAVORITE)
                .where(
                    FAVORITE.KIND.eq(row.kind())
                        .and(FAVORITE.ALBUM_NAME.eq(row.albumName()))
                        .and(FAVORITE.ALBUM_ARTIST.eq(row.albumArtist()))
                        .and(FAVORITE.TRACK_PATH.eq(row.trackPath()))
                )
                .execute()
        );
    }

    // Column values of a favorite. Columns that do not apply to the kind
    // hold an empty string, because the columns are NOT NULL.
    private record Row(String kind, String albumName, String albumArtist, String trackPath) {
        static Row of(Favorite favorite) {
            return switch (favorite) {
                case FavoriteAlbum album -> new Row(ALBUM_KIND, album.name(), album.artist(), "");
                case FavoriteTrack track -> new Row(TRACK_KIND, "", "", track.path().toString());
            };
        }
    }
}
