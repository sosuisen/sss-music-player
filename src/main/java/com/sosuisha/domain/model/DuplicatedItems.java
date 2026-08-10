package com.sosuisha.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * A group of files that are considered duplicates of each other.
 *
 * @param title display title of the group
 * @param files duplicated audio files
 */
public record DuplicatedItems(String title, List<MusicFile> files) {
    /**
     * Creates the group.
     *
     * @throws NullPointerException if title or files is null
     */
    public DuplicatedItems {
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(files, "files must not be null");
    }
}
