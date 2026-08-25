package com.sosuisha.domain.service;

import java.nio.file.Path;

import com.sosuisha.domain.exception.FolderOpenException;

/**
 * Opens folders in the file manager of the platform.
 */
@FunctionalInterface
public interface FolderOpener {
    /**
     * Opens the given folder.
     *
     * @param folder path of the folder to open
     * @throws FolderOpenException if the folder cannot be opened
     */
    void open(Path folder) throws FolderOpenException;
}
