package com.sosuisha.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.sosuisha.domain.exception.DuplicateRemovalException;
import com.sosuisha.domain.model.DuplicatedItems;

/**
 * Moves duplicated files out of the music library into a duplicates folder.
 * Every move is appended to a log file as a
 * {@code date,source path,destination file name} line.
 */
public class DuplicateFileMover {
    private final Path duplicatesFolder;
    private final Path logFile;

    /**
     * Creates the mover.
     *
     * @param duplicatesFolder folder that receives the moved files
     * @param logFile file that receives a log line for every moved file
     * @throws NullPointerException if duplicatesFolder or logFile is null
     */
    public DuplicateFileMover(Path duplicatesFolder, Path logFile) {
        this.duplicatesFolder =
            Objects.requireNonNull(duplicatesFolder, "duplicatesFolder must not be null");
        this.logFile = Objects.requireNonNull(logFile, "logFile must not be null");
    }

    /**
     * Moves all but the first file of each group into the duplicates folder
     * and logs every move. The folder is created when it does not exist.
     *
     * @param groups groups of duplicated files
     * @throws NullPointerException if groups is null
     * @throws DuplicateRemovalException if the duplicates folder cannot be
     *             created, a file cannot be moved, or the log cannot be written
     */
    public void moveDuplicates(List<DuplicatedItems> groups) throws DuplicateRemovalException {
        Objects.requireNonNull(groups, "groups must not be null");
        createDuplicatesFolder();
        for (var group : groups) {
            for (var file : group.files().subList(1, group.files().size())) {
                var destination = resolveDestination(file.path().getFileName());
                move(file.path(), destination);
                log(file.path(), destination);
            }
        }
    }

    private void createDuplicatesFolder() throws DuplicateRemovalException {
        try {
            Files.createDirectories(duplicatesFolder);
        } catch (IOException e) {
            throw new DuplicateRemovalException(
                "Could not create the duplicates folder: " + duplicatesFolder, e
            );
        }
    }

    private static void move(Path source, Path destination) throws DuplicateRemovalException {
        try {
            Files.move(source, destination);
        } catch (IOException e) {
            throw new DuplicateRemovalException("Could not move the duplicate file: " + source, e);
        }
    }

    private Path resolveDestination(Path fileName) {
        var name = fileName.toString();
        var dotIndex = name.lastIndexOf('.');
        var base = dotIndex < 0 ? name : name.substring(0, dotIndex);
        var extension = dotIndex < 0 ? "" : name.substring(dotIndex);
        var candidate = duplicatesFolder.resolve(fileName);
        for (var i = 1; Files.exists(candidate); i++) {
            candidate = duplicatesFolder.resolve(base + "_" + i + extension);
        }
        return candidate;
    }

    private void log(Path source, Path destination) throws DuplicateRemovalException {
        var line = LocalDate.now() + "," + source + "," + destination.getFileName()
            + System.lineSeparator();
        try {
            Files.writeString(logFile, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new DuplicateRemovalException(
                "Could not write the duplicates log: " + logFile, e
            );
        }
    }
}
