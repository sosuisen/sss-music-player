package com.sosuisha.repository;

import static com.sosuisha.db.Tables.TRACK;

import java.io.IOException;
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
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import com.sosuisha.domain.exception.RepositoryException;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.TrackMetadata;
import com.sosuisha.domain.service.LibraryRepository;

/**
 * Library database stored in a SQLite file, accessed through jOOQ.
 * <p>
 * I/O errors are reported as runtime exceptions, in line with jOOQ, which
 * wraps every {@code SQLException} in its unchecked {@code DataAccessException}.
 * This class translates both into {@link RepositoryException} so that
 * callers see one exception type and jOOQ types do not leak out.
 */
public class SqliteLibraryRepository implements LibraryRepository {
    public static final Path DEFAULT_FILE =
        Path.of(System.getProperty("user.home"), ".sss-music-player", "library.db");

    private static final String SCHEMA_RESOURCE = "/db/schema.sql";

    private final Path file;
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
     * @throws RepositoryException if the database cannot be opened
     */
    public SqliteLibraryRepository() throws RepositoryException {
        this(resolveFile());
    }

    /**
     * Creates the database on the given SQLite file. The file, its parent
     * folder, and the schema are created when they do not exist.
     *
     * @param file path of the SQLite database file
     * @throws NullPointerException  if file is null
     * @throws RepositoryException if the parent folder cannot be created or the
     *             database cannot be opened
     */
    public SqliteLibraryRepository(Path file) throws RepositoryException {
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
        runWithDsl(
            "Could not initialize the library database", dsl -> dsl.execute(loadSchemaSql())
        );
    }

    private static String loadSchemaSql() {
        try (var in = SqliteLibraryRepository.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RepositoryException("Could not read the schema of the library database", e);
        }
    }

    private <T> T withDsl(String errorMessage, Function<DSLContext, T> operation) {
        try (var connection = DriverManager.getConnection(url)) {
            return operation.apply(DSL.using(connection, SQLDialect.SQLITE));
        } catch (SQLException | DataAccessException e) {
            throw new RepositoryException(errorMessage + ": " + file, e);
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
     * @throws RepositoryException if the database cannot be read
     */
    @Override
    public Optional<TrackMetadata> find(Path path, long size, FileTime lastModified)
        throws RepositoryException {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(lastModified, "lastModified must not be null");
        return withDsl(
            "Could not read the library database", dsl -> dsl
                .selectFrom(TRACK)
                .where(
                    TRACK.PATH.eq(path.toString())
                        .and(TRACK.SIZE.eq(size))
                        .and(TRACK.LAST_MODIFIED.ge(lastModified.toMillis()))
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
     * @throws RepositoryException if the database cannot be written
     */
    @Override
    public void save(MusicFile file, FileTime lastModified) throws RepositoryException {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(lastModified, "lastModified must not be null");
        runWithDsl("Could not write to the library database", dsl -> {
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
     * @throws RepositoryException if the database cannot be read
     */
    @Override
    public List<Path> findAllPaths() throws RepositoryException {
        return withDsl(
            "Could not read the library database",
            dsl -> dsl.select(TRACK.PATH).from(TRACK).fetch(record -> Path.of(record.value1()))
        );
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException  if path is null
     * @throws RepositoryException if the database cannot be written
     */
    @Override
    public void delete(Path path) throws RepositoryException {
        Objects.requireNonNull(path, "path must not be null");
        runWithDsl(
            "Could not write to the library database",
            dsl -> dsl.deleteFrom(TRACK).where(TRACK.PATH.eq(path.toString())).execute()
        );
    }

}
