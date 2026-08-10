package com.sosuisha.presentation.appmodel;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import com.sosuisha.service.LibraryScanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

/**
 * Application-wide state of the music library shared by multiple screens.
 */
public class MusicLibraryAppModel {
    private final ObservableList<Path> files = FXCollections.observableArrayList();
    private final LibraryScanner scanner;

    /**
     * Creates the app model. It follows the settings of the given settings app
     * model: whenever the settings hold a music library path (at creation and
     * on every change), that folder is scanned and the list of audio files is
     * updated.
     *
     * @param scanner scanner that lists the audio files in a library folder
     * @param settingsAppModel application-wide state of the settings
     * @throws NullPointerException if scanner or settingsAppModel is null
     */
    public MusicLibraryAppModel(LibraryScanner scanner, SettingsAppModel settingsAppModel) {
        this.scanner = Objects.requireNonNull(scanner, "scanner must not be null");
        Objects.requireNonNull(settingsAppModel, "settingsAppModel must not be null");
        settingsAppModel.settingsProperty().subscribe(settings -> {
            if (settings != null) {
                scanFolder(settings.musicLibraryPath());
            }
        });
    }

    /**
     * Scans the given folder in the background and replaces the list of audio
     * files with the result when the scan finishes. Returns immediately; the
     * list is updated on the JavaFX application thread.
     *
     * @param folderPath path of the folder to scan
     * @throws NullPointerException if folderPath is null
     */
    public void scanFolder(Path folderPath) {
        Objects.requireNonNull(folderPath, "folderPath must not be null");
        var task = new Task<List<Path>>() {
            @Override
            protected List<Path> call() throws Exception {
                return scanner.scan(folderPath);
            }
        };
        task.setOnSucceeded(_ -> files.setAll(task.getValue()));
        Thread.ofVirtual().start(task);
    }

    /**
     * Sets the list of audio files in the library. The observable list instance
     * is kept; its contents are replaced.
     *
     * @param files list of audio file paths
     * @throws NullPointerException if files is null
     */
    public void setFiles(List<Path> files) {
        Objects.requireNonNull(files, "files must not be null");
        this.files.setAll(files);
    }

    /**
     * Returns the list of audio files in the library.
     *
     * @return observable list of audio file paths
     */
    public ObservableList<Path> getFiles() {
        return files;
    }
}
