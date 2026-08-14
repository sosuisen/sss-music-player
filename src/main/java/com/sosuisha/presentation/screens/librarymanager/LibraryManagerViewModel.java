package com.sosuisha.presentation.screens.librarymanager;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.sosuisha.domain.model.Album;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.presentation.WindowManager;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListView;
import com.sosuisha.presentation.screens.settings.SettingsView;
import com.sosuisha.service.AlbumDetector;

import io.github.sosuisen.jfxbuilder.graphics.StageBuilder;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Modality;

/**
 * ViewModel for the library manager screen.
 */
public class LibraryManagerViewModel {
    private final WindowManager windowManager;
    private final MusicLibraryAppModel appModel;
    private final ObservableList<Album> albums = FXCollections.observableArrayList();
    private final ObservableList<MusicFile> selectedTracks = FXCollections.observableArrayList();

    /**
     * Creates the view model.
     *
     * @param windowManager window manager used to open other windows
     * @param appModel application-wide state of the music library
     * @throws NullPointerException if windowManager or appModel is null
     */
    public LibraryManagerViewModel(WindowManager windowManager, MusicLibraryAppModel appModel) {
        this.windowManager =
            Objects.requireNonNull(windowManager, "windowManager must not be null");
        this.appModel = Objects.requireNonNull(appModel, "appModel must not be null");
        appModel.getFiles().subscribe(this::updateAlbums);
        updateAlbums();
    }

    private void updateAlbums() {
        albums.setAll(new AlbumDetector(appModel.getFiles()).detect());
    }

    /**
     * Rescans the music library.
     */
    public void rescan() {
        appModel.rescan();
    }

    /**
     * Opens the duplicate file list window.
     */
    public void openDuplicateListWindow() {
        windowManager.showWindow(DuplicateListView.class, StageBuilder.create().build());
    }

    /**
     * Opens the settings window. The window is application modal.
     */
    public void openSettingsWindow() {
        windowManager.showWindow(
            SettingsView.class,
            StageBuilder.create()
                .apply(stage -> stage.initModality(Modality.APPLICATION_MODAL))
                .build()
        );
    }

    /**
     * Sets the list of audio files in the library.
     *
     * @param files list of audio files
     * @throws NullPointerException if files is null
     */
    public void setFiles(List<MusicFile> files) {
        appModel.setFiles(files);
    }

    /**
     * Returns the list of audio files in the library.
     *
     * @return observable list of audio files
     */
    public ObservableList<MusicFile> getFiles() {
        return appModel.getFiles();
    }

    /**
     * Returns the albums recognized in the music library. The list is updated
     * whenever the files of the library change.
     *
     * @return observable list of the albums
     */
    public ObservableList<Album> getAlbums() {
        return albums;
    }

    /**
     * Selects the given album. The tracks of the album are published through
     * {@link #getSelectedTracks()} in track number order.
     *
     * @param album album to select, or null to clear the selection
     */
    public void selectAlbum(Album album) {
        selectedTracks.setAll(album == null ? List.of() : orderByTrackNumber(album.files()));
    }

    /**
     * Returns the tracks of the selected album in track number order. The
     * list is empty when no album is selected.
     *
     * @return observable list of the tracks of the selected album
     */
    public ObservableList<MusicFile> getSelectedTracks() {
        return selectedTracks;
    }

    private static List<MusicFile> orderByTrackNumber(List<MusicFile> files) {
        return files.stream()
            .sorted(Comparator.comparingInt(LibraryManagerViewModel::trackNumberOf))
            .toList();
    }

    private static int trackNumberOf(MusicFile file) {
        try {
            return Integer.parseInt(file.tag().trackNumber());
        } catch (NumberFormatException e) {
            // A track without a readable number goes last.
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Returns whether a library scan is running.
     *
     * @return read-only property that is true while a scan is running
     */
    public ReadOnlyBooleanProperty scanningProperty() {
        return appModel.scanningProperty();
    }

    /**
     * Returns the path of the file that the running library scan is reading.
     *
     * @return read-only property holding the path of the file being read
     */
    public ReadOnlyStringProperty scanningFileProperty() {
        return appModel.scanningFileProperty();
    }
}
