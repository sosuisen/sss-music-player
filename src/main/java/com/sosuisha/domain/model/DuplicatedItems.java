package com.sosuisha.domain.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * A group of files that are considered duplicates of each other.
 *
 * @param title display title of the group
 * @param paths paths of the duplicated files
 */
public record DuplicatedItems(String title, List<Path> paths) {
    /**
     * Creates the group.
     *
     * @throws NullPointerException if title or paths is null
     */
    public DuplicatedItems {
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(paths, "paths must not be null");
    }
}
