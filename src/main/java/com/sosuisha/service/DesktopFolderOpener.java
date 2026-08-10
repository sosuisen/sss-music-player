package com.sosuisha.service;

import java.awt.Desktop;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;

import com.sosuisha.domain.service.FolderOpener;

/**
 * Opens folders in the file manager of the platform with the AWT desktop
 * integration.
 */
public class DesktopFolderOpener implements FolderOpener {
    /**
     * Opens the given folder.
     *
     * @param folder path of the folder to open
     * @throws NullPointerException if folder is null
     * @throws UncheckedIOException if the folder cannot be opened
     */
    @Override
    public void open(Path folder) {
        Objects.requireNonNull(folder, "folder must not be null");
        try {
            Desktop.getDesktop().open(folder.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
