package com.sosuisha.presentation.screens.librarymanager;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.sosuisha.domain.model.Album;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.service.MusicPlayer;
import com.sosuisha.presentation.WindowManager;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListView;
import com.sosuisha.presentation.screens.settings.SettingsView;
import com.sosuisha.service.AlbumDetector;

import io.github.sosuisen.jfxbuilder.graphics.StageBuilder;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Modality;

/**
 * ViewModel for the library manager screen.
 */
public class LibraryManagerViewModel {
    private final WindowManager windowManager;
    private final MusicLibraryAppModel appModel;
    private final MusicPlayer musicPlayer;
    private final ObservableList<Album> albums = FXCollections.observableArrayList();
    private final ObservableList<MusicFile> selectedTracks = FXCollections.observableArrayList();
    private final ObjectProperty<PlayerState> playerState =
        new SimpleObjectProperty<>(PlayerState.STOPPED);
    private final ObjectProperty<MusicFile> selectedTrack = new SimpleObjectProperty<>();
    private MusicFile playingTrack;

    /**
     * Creates the view model.
     *
     * @param windowManager window manager used to open other windows
     * @param appModel application-wide state of the music library
     * @param musicPlayer player used to play audio files
     * @throws NullPointerException if windowManager, appModel, or musicPlayer is null
     */
    public LibraryManagerViewModel(WindowManager windowManager, MusicLibraryAppModel appModel,
        MusicPlayer musicPlayer) {
        this.windowManager =
            Objects.requireNonNull(windowManager, "windowManager must not be null");
        this.appModel = Objects.requireNonNull(appModel, "appModel must not be null");
        this.musicPlayer = Objects.requireNonNull(musicPlayer, "musicPlayer must not be null");
        musicPlayer.setOnFinished(this::playNextWhenTrackFinishes);
        appModel.getFiles().subscribe(this::updateAlbums);
        updateAlbums();
    }

    private void playNextWhenTrackFinishes() {
        if (playerState.get() == PlayerState.PLAYING) {
            nextTrack();
        }
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

    /**
     * Selects the given track as the playback target.
     *
     * @param track track to select, or null to clear the selection
     */
    public void selectTrack(MusicFile track) {
        selectedTrack.set(track);
    }

    /**
     * Returns the track selected as the playback target.
     *
     * @return read-only property of the selected track
     */
    public ReadOnlyObjectProperty<MusicFile> selectedTrackProperty() {
        return selectedTrack;
    }

    /**
     * Moves the selection to the next track of the selected album, wrapping
     * to the first track at the end. When a track is playing, the moved-to
     * track is played immediately. Does nothing when the album has no tracks.
     */
    public void nextTrack() {
        moveSelection(1);
    }

    /**
     * Moves the selection to the previous track of the selected album,
     * wrapping to the last track at the head. When a track is playing, the
     * moved-to track is played immediately. Does nothing when the album has
     * no tracks.
     */
    public void previousTrack() {
        moveSelection(-1);
    }

    private void moveSelection(int offset) {
        if (selectedTracks.isEmpty()) { return; }
        var index = selectedTracks.indexOf(selectedTrack.get());
        var size = selectedTracks.size();
        var movedTo = selectedTracks.get((index + offset + size) % size);
        selectedTrack.set(movedTo);
        if (playerState.get() == PlayerState.PLAYING) {
            musicPlayer.play(movedTo.path());
            playingTrack = movedTo;
        }
    }

    /**
     * Plays the selected track, pauses the playback when a track is playing,
     * or resumes the paused playback. Does nothing when nothing is playing and
     * no track is selected.
     */
    public void togglePlay() {
        switch (playerState.get()) {
            case PLAYING -> {
                musicPlayer.pause();
                playerState.set(PlayerState.PAUSED);
            }
            case PAUSED -> {
                if (Objects.equals(selectedTrack.get(), playingTrack)) {
                    musicPlayer.resume();
                } else {
                    // The selection moved while paused, so the resume target
                    // is gone; the selected track starts from the beginning.
                    musicPlayer.play(selectedTrack.get().path());
                    playingTrack = selectedTrack.get();
                }
                playerState.set(PlayerState.PLAYING);
            }
            case STOPPED -> {
                if (selectedTrack.get() != null) {
                    musicPlayer.play(selectedTrack.get().path());
                    playingTrack = selectedTrack.get();
                    playerState.set(PlayerState.PLAYING);
                }
            }
        }
    }

    /**
     * Returns the state of the playback.
     *
     * @return read-only property of the player state
     */
    public ReadOnlyObjectProperty<PlayerState> playerStateProperty() {
        return playerState;
    }

    /**
     * Stops the playback and moves the player state to
     * {@link PlayerState#STOPPED}.
     */
    public void stopPlayback() {
        musicPlayer.stop();
        playerState.set(PlayerState.STOPPED);
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
