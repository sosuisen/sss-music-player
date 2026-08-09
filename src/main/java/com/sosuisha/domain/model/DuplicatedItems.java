package com.sosuisha.domain.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * A group of files that are considered duplicates of each other.
 *
 * @param paths paths of the duplicated files
 */
public record DuplicatedItems(List<Path> paths) {
    /**
     * Creates the group.
     *
     * @throws NullPointerException if paths is null
     */
    public DuplicatedItems {
        Objects.requireNonNull(paths, "paths must not be null");
    }
}
