package com.sosuisha.presentation.screens.librarymanager;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;

import javafx.collections.ObservableList;

/**
 * ViewModel for the library manager screen.
 */
public class LibraryManagerViewModel {
    private final MusicLibraryAppModel appModel;

    /**
     * Creates the view model.
     *
     * @param appModel application-wide state of the music library
     * @throws NullPointerException if appModel is null
     */
    public LibraryManagerViewModel(MusicLibraryAppModel appModel) {
        this.appModel = Objects.requireNonNull(appModel, "appModel must not be null");
    }

    /**
     * Sets the list of audio files in the library.
     *
     * @param files list of audio file paths
     * @throws NullPointerException if files is null
     */
    public void setFiles(List<Path> files) {
        appModel.setFiles(files);
    }

    /**
     * Returns the list of audio files in the library.
     *
     * @return observable list of audio file paths
     */
    public ObservableList<Path> getFiles() {
        return appModel.getFiles();
    }
}
