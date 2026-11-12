package com.sosuisha.service;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Failure of reading the tag of one audio file during a library scan.
 *
 * @param path path of the file whose tag could not be read
 * @param error error that stopped the read
 */
public record TagReadFailure(Path path, LinkageError error) {
    /**
     * Creates the failure.
     *
     * @throws NullPointerException if path or error is null
     */
    public TagReadFailure {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(error, "error must not be null");
    }
}
