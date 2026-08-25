package com.sosuisha.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

import com.sosuisha.domain.exception.FolderOpenException;
import com.sosuisha.domain.service.FolderOpener;

/**
 * Opens folders in the file manager of the platform by launching the shell
 * command of the platform (explorer, open, or xdg-open). It works on every
 * platform without the AWT desktop integration.
 */
public class ShellFolderOpener implements FolderOpener {
    private final String command;

    /**
     * Creates the opener with the file manager command of the platform:
     * explorer.exe on Windows, open on macOS, and xdg-open elsewhere.
     */
    public ShellFolderOpener() {
        this(platformCommand());
    }

    /**
     * Creates the opener with the given file manager command. The folder path
     * is passed to the command as its only argument.
     *
     * @param command file manager command to launch
     * @throws NullPointerException if command is null
     */
    public ShellFolderOpener(String command) {
        this.command = Objects.requireNonNull(command, "command must not be null");
    }

    /**
     * Opens the given folder.
     *
     * @param folder path of the folder to open
     * @throws NullPointerException if folder is null
     * @throws FolderOpenException if the file manager cannot be launched
     */
    @Override
    public void open(Path folder) throws FolderOpenException {
        Objects.requireNonNull(folder, "folder must not be null");
        try {
            new ProcessBuilder(command, folder.toString()).start();
        } catch (IOException e) {
            throw new FolderOpenException("Could not open the folder: " + folder, e);
        }
    }

    private static String platformCommand() {
        var os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) { return "explorer.exe"; }
        if (os.contains("mac")) { return "open"; }
        return "xdg-open";
    }
}
