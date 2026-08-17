package com.sosuisha.presentation.appmodel;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import com.sosuisha.domain.model.Album;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.service.LibraryIndexer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

/**
 * Application-wide state of the music library shared by multiple screens.
 */
public class MusicLibraryAppModel {
    private final ObservableList<MusicFile> files = FXCollections.observableArrayList();
    private final BooleanProperty scanning = new SimpleBooleanProperty(false);
    private final StringProperty scanningFile = new SimpleStringProperty("");
    private final ObjectProperty<Album> selectedAlbum = new SimpleObjectProperty<>();
    private final LibraryIndexer scanner;
    private Path lastScannedFolder;

    /**
     * Creates the app model. It follows the given music library path: whenever
     * it holds a path (at creation and on every path change), that folder is
     * scanned and the list of audio files is updated.
     *
     * @param scanner scanner that lists the audio files in a library folder
     * @param musicLibraryPath observable path of the music library folder,
     *            holding null while no folder has been chosen
     * @throws NullPointerException if scanner or musicLibraryPath is null
     */
    public MusicLibraryAppModel(LibraryIndexer scanner, ObservableValue<Path> musicLibraryPath) {
        this.scanner = Objects.requireNonNull(scanner, "scanner must not be null");
        Objects.requireNonNull(musicLibraryPath, "musicLibraryPath must not be null");
        musicLibraryPath.subscribe(path -> {
            if (path != null) {
                scanFolder(path);
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
        lastScannedFolder = folderPath;
        scanning.set(true);
        var task = new Task<List<MusicFile>>() {
            @Override
            protected List<MusicFile> call() throws Exception {
                // updateMessage publishes the value to messageProperty on the
                // FX thread, coalescing rapid updates.
                return scanner.scan(folderPath, path -> updateMessage(path.toString()));
            }
        };
        scanningFile.bind(task.messageProperty());
        task.setOnSucceeded(_ -> {
            files.setAll(task.getValue());
            scanning.set(false);
        });
        task.setOnFailed(_ -> scanning.set(false));
        Thread.ofVirtual().start(task);
    }

    /**
     * Scans the last scanned folder again in the background. Does nothing when
     * no folder has been scanned yet.
     */
    public void rescan() {
        if (lastScannedFolder != null) {
            scanFolder(lastScannedFolder);
        }
    }

    /**
     * Sets the list of audio files in the library. The observable list instance
     * is kept; its contents are replaced.
     *
     * @param files list of audio files
     * @throws NullPointerException if files is null
     */
    public void setFiles(List<MusicFile> files) {
        Objects.requireNonNull(files, "files must not be null");
        this.files.setAll(files);
    }

    /**
     * Returns the list of audio files in the library.
     *
     * @return observable list of audio files
     */
    public ObservableList<MusicFile> getFiles() {
        return files;
    }

    /**
     * Selects the given album as the album shared by the screens.
     *
     * @param album album to select, or null to clear the selection
     */
    public void selectAlbum(Album album) {
        selectedAlbum.set(album);
    }

    /**
     * Returns the selected album.
     *
     * @return read-only property of the selected album, holding null when no
     *     album is selected
     */
    public ReadOnlyObjectProperty<Album> selectedAlbumProperty() {
        return selectedAlbum;
    }

    /**
     * Returns whether a scan is running.
     *
     * @return read-only property that is true while a scan is running
     */
    public ReadOnlyBooleanProperty scanningProperty() {
        return scanning;
    }

    /**
     * Returns the path of the file that the running scan is reading.
     *
     * @return read-only property holding the path of the file being read
     */
    public ReadOnlyStringProperty scanningFileProperty() {
        return scanningFile;
    }
}
