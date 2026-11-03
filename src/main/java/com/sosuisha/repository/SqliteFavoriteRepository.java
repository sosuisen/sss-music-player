package com.sosuisha.repository;

import static com.sosuisha.db.Tables.FAVORITE;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import com.sosuisha.db.tables.records.FavoriteRecord;
import com.sosuisha.domain.exception.RepositoryException;
import com.sosuisha.domain.model.Favorite;
import com.sosuisha.domain.model.FavoriteAlbum;
import com.sosuisha.domain.model.FavoriteTrack;
import org.jspecify.annotations.Nullable;

/**
 * Favorites stored in the SQLite library database.
 */
public class SqliteFavoriteRepository {
    private static final String SCHEMA_RESOURCE = "/db/schema.sql";
    private static final String ALBUM_KIND = "album";
    private static final String TRACK_KIND = "track";

    private final Path file;
    private final String url;

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
        createParentFolder(file);
        this.file = file;
        this.url = "jdbc:sqlite:" + file;
        createSchema();
    }

    private static void createParentFolder(Path file) {
        var parent = file.getParent();
        if (parent == null) { return; }
        try {
            Files.createDirectories(parent);
        } catch (IOException e) {
            throw new RepositoryException(
                "Could not create the folder for the library database: " + parent, e
            );
        }
    }

    private void createSchema() {
        runWithDsl("Could not initialize the library database", dsl -> {
            for (var statement : loadSchemaSql().split(";")) {
                if (!statement.isBlank()) {
                    dsl.execute(statement);
                }
            }
        });
    }

    private static String loadSchemaSql() {
        try (var in = SqliteFavoriteRepository.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RepositoryException("Could not read the schema of the library database", e);
        }
    }

    private <T extends @Nullable Object> T withDsl(
        String errorMessage, Function<DSLContext, T> operation) {
        try (var connection = DriverManager.getConnection(url)) {
            return operation.apply(DSL.using(connection, SQLDialect.SQLITE));
        } catch (SQLException | DataAccessException e) {
            throw new RepositoryException(errorMessage + ": " + file, e);
        }
    }

    private void runWithDsl(String errorMessage, Consumer<DSLContext> operation) {
        this.<@Nullable Void>withDsl(errorMessage, dsl -> {
            operation.accept(dsl);
            return null;
        });
    }

    /**
     * Returns all favorites in the order they were added.
     *
     * @return list of the favorites
     * @throws RepositoryException if the database cannot be read
     */
    public List<Favorite> findAll() {
        return withDsl(
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
        runWithDsl("Could not write to the library database", dsl -> {
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
}
