package com.sosuisha.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.sosuisha.domain.service.FolderOpener;

/**
 * Opens folders in the file manager of the platform by launching the shell
 * command of the platform (explorer, open, or xdg-open). It works on every
 * platform without the AWT desktop integration.
 */
public class ShellFolderOpener implements FolderOpener {
    /**
     * Opens the given folder.
     *
     * @param folder path of the folder to open
     * @throws NullPointerException if folder is null
     * @throws UncheckedIOException if the file manager cannot be launched
     */
    @Override
    public void open(Path folder) {
        Objects.requireNonNull(folder, "folder must not be null");
        try {
            new ProcessBuilder(commandFor(folder)).start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<String> commandFor(Path folder) {
        var os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) { return List.of("explorer.exe", folder.toString()); }
        if (os.contains("mac")) { return List.of("open", folder.toString()); }
        return List.of("xdg-open", folder.toString());
    }
}
