package com.sosuisha.presentation.screens.librarymanager;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the library manager screen.
 */
public class LibraryManagerViewModel {
    private final ObservableList<Path> files = FXCollections.observableArrayList();

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
