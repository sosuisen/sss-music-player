package com.sosuisha.repository;

import static com.sosuisha.db.Tables.TRACK;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.sosuisha.domain.exception.RepositoryException;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.TrackMetadata;
import com.sosuisha.domain.repository.LibraryRepository;

/**
 * Library database stored in a SQLite file, accessed through jOOQ.
 * <p>
 * I/O errors are reported as {@link RepositoryException}, a runtime
 * exception, in line with jOOQ.
 */
public class SqliteLibraryRepository implements LibraryRepository {
    public static final Path DEFAULT_FILE =
        Path.of(System.getProperty("user.home"), ".sss-music-player", "library.db");

    private final SqliteDatabase database;

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
        this.database = new SqliteDatabase(file);
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
        return database.query(
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
        database.run("Could not write to the library database", dsl -> {
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
        return database.query(
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
        database.run(
            "Could not write to the library database",
            dsl -> dsl.deleteFrom(TRACK).where(TRACK.PATH.eq(path.toString())).execute()
        );
    }

}
