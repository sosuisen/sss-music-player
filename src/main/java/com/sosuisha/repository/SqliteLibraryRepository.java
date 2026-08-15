package com.sosuisha.repository;

import static com.sosuisha.db.Tables.TRACK;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.TrackMetadata;
import com.sosuisha.domain.service.LibraryDatabase;

/**
 * Library database stored in a SQLite file, accessed through jOOQ.
 */
public class SqliteLibraryRepository implements LibraryDatabase {
    /** Default database file: {@code ~/.sss-music-player/library.db}. */
    public static final Path DEFAULT_FILE =
        Path.of(System.getProperty("user.home"), ".sss-music-player", "library.db");

    private static final String SCHEMA_RESOURCE = "/db/schema.sql";

    private final String url;

    /**
     * Resolves the path of the SQLite database file. The system property
     * {@code sss.library.db} takes precedence.
     *
     * @return path of the SQLite database file
     */
    static Path resolveFile() {
        var override = System.getProperty("sss.library.db");
        if (override != null) { return Path.of(override); }
        return DEFAULT_FILE;
    }

    /**
     * Creates the database on the SQLite file resolved by {@link #resolveFile()}.
     *
     * @throws IllegalStateException if the database cannot be opened
     */
    public SqliteLibraryRepository() {
        this(resolveFile());
    }

    /**
     * Creates the database on the given SQLite file. The file, its parent
     * folder, and the schema are created when they do not exist.
     *
     * @param file path of the SQLite database file
     * @throws NullPointerException  if file is null
     * @throws UncheckedIOException  if the parent folder cannot be created
     * @throws IllegalStateException if the database cannot be opened
     */
    public SqliteLibraryRepository(Path file) {
        Objects.requireNonNull(file, "file must not be null");
        createParentFolder(file);
        this.url = "jdbc:sqlite:" + file;
        createSchema();
    }

    private static void createParentFolder(Path file) {
        var parent = file.getParent();
        if (parent == null) { return; }
        try {
            Files.createDirectories(parent);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void createSchema() {
        runWithDsl("cannot create the database schema", dsl -> dsl.execute(loadSchemaSql()));
    }

    private static String loadSchemaSql() {
        try (var in = SqliteLibraryRepository.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private <T> T withDsl(String errorMessage, Function<DSLContext, T> operation) {
        try (var connection = DriverManager.getConnection(url)) {
            return operation.apply(DSL.using(connection, SQLDialect.SQLITE));
        } catch (SQLException e) {
            throw new IllegalStateException(errorMessage, e);
        }
    }

    private void runWithDsl(String errorMessage, Consumer<DSLContext> operation) {
        withDsl(errorMessage, dsl -> {
            operation.accept(dsl);
            return null;
        });
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException  if path or lastModified is null
     * @throws IllegalStateException if the database cannot be read
     */
    @Override
    public Optional<TrackMetadata> find(Path path, long size, FileTime lastModified) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(lastModified, "lastModified must not be null");
        return withDsl(
            "cannot read the database", dsl -> dsl
                .selectFrom(TRACK)
                .where(
                    TRACK.PATH.eq(path.toString())
                        .and(TRACK.SIZE.eq(size))
                        .and(TRACK.LAST_MODIFIED.eq(lastModified.toMillis()))
                )
                .fetchOptional()
                .map(
                    record -> new TrackMetadata(
                        record.getTitle(), record.getArtist(), record.getAlbum(),
                        record.getAlbumArtist(), record.getTrackNumber(), record.getReleaseYear()
                    )
                )
        );
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException  if file or lastModified is null
     * @throws IllegalStateException if the database cannot be written
     */
    @Override
    public void save(MusicFile file, FileTime lastModified) {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(lastModified, "lastModified must not be null");
        runWithDsl("cannot write to the database", dsl -> {
            var record = dsl.newRecord(TRACK);
            record.setPath(file.path().toString());
            record.setSize(file.size());
            record.setLastModified(lastModified.toMillis());
            record.setTitle(file.tag().title());
            record.setArtist(file.tag().artist());
            record.setAlbum(file.tag().album());
            record.setAlbumArtist(file.tag().albumArtist());
            record.setTrackNumber(file.tag().trackNumber());
            record.setReleaseYear(file.tag().year());
            record.merge(); // upsert
        });
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if the database cannot be read
     */
    @Override
    public List<Path> findAllPaths() {
        return withDsl(
            "cannot read the database",
            dsl -> dsl.select(TRACK.PATH).from(TRACK).fetch(record -> Path.of(record.value1()))
        );
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException  if path is null
     * @throws IllegalStateException if the database cannot be written
     */
    @Override
    public void delete(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        runWithDsl(
            "cannot write to the database",
            dsl -> dsl.deleteFrom(TRACK).where(TRACK.PATH.eq(path.toString())).execute()
        );
    }

}
