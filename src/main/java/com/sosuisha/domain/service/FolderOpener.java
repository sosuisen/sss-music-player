package com.sosuisha.domain.service;

import java.nio.file.Path;

/**
 * Opens folders in the file manager of the platform.
 */
@FunctionalInterface
public interface FolderOpener {
    /**
     * Opens the given folder.
     *
     * @param folder path of the folder to open
     */
    void open(Path folder);
}
