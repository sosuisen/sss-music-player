package com.sosuisha.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import com.sosuisha.domain.exception.RepositoryException;
import org.jspecify.annotations.Nullable;

/**
 * The SQLite library database file shared by the repositories. Each operation
 * opens its own connection.
 * <p>
 * jOOQ wraps every {@code SQLException} in its unchecked
 * {@code DataAccessException}. This class translates both into
 * {@link RepositoryException} so that jOOQ types do not leak out.
 */
class SqliteDatabase {
    private static final String SCHEMA_RESOURCE = "/db/schema.sql";

    private final Path file;
    private final String url;

    /**
     * Opens the database on the given SQLite file. The file, its parent
     * folder, and the schema are created when they do not exist.
     *
     * @param file path of the SQLite database file
     * @throws NullPointerException if file is null
     * @throws RepositoryException if the parent folder cannot be created or the
     *             database cannot be opened
     */
    SqliteDatabase(Path file) {
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
        // The JDBC driver runs only the first statement of a string, so the
        // statements of the schema run one by one.
        run("Could not initialize the library database", dsl -> {
            for (var statement : loadSchemaSql().split(";")) {
                if (!statement.isBlank()) {
                    dsl.execute(statement);
                }
            }
        });
    }

    private static String loadSchemaSql() {
        try (var in = SqliteDatabase.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RepositoryException("Could not read the schema of the library database", e);
        }
    }

    /**
     * Runs the given query on a new connection and returns its result.
     *
     * @param <T> type of the result
     * @param errorMessage message of the exception thrown on failure, followed
     *            by the path of the database file
     * @param operation query that uses the connection
     * @return result of the query
     * @throws RepositoryException if the database cannot be accessed
     */
    <T extends @Nullable Object> T query(String errorMessage, Function<DSLContext, T> operation) {
        try (var connection = DriverManager.getConnection(url)) {
            return operation.apply(DSL.using(connection, SQLDialect.SQLITE));
        } catch (SQLException | DataAccessException e) {
            throw new RepositoryException(errorMessage + ": " + file, e);
        }
    }

    /**
     * Runs the given statement on a new connection.
     *
     * @param errorMessage message of the exception thrown on failure, followed
     *            by the path of the database file
     * @param operation statement that uses the connection
     * @throws RepositoryException if the database cannot be accessed
     */
    void run(String errorMessage, Consumer<DSLContext> operation) {
        this.<@Nullable Void>query(errorMessage, dsl -> {
            operation.accept(dsl);
            return null;
        });
    }
}
