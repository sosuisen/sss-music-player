package com.sosuisha.presentation.screens.librarymanager;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import com.sosuisha.domain.model.Album;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.service.FolderOpener;
import com.sosuisha.domain.service.MusicPlayer;
import com.sosuisha.presentation.WindowManager;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.screens.albumedit.AlbumEditView;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListView;
import com.sosuisha.presentation.screens.settings.SettingsView;
import com.sosuisha.service.AlbumDetector;

import io.github.sosuisen.jfxbuilder.graphics.StageBuilder;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
    private final FolderOpener folderOpener;
    private final ObservableList<Album> albums = FXCollections.observableArrayList();
    private final ObservableList<MusicFile> selectedTracks = FXCollections.observableArrayList();
    private final ObjectProperty<PlayerState> playerState =
        new SimpleObjectProperty<>(PlayerState.STOPPED);
    private final ObjectProperty<MusicFile> selectedTrack = new SimpleObjectProperty<>();
    private final ObjectProperty<SortKey> sortKey = new SimpleObjectProperty<>(SortKey.ALBUM);
    private final ObjectProperty<RepeatMode> repeatMode =
        new SimpleObjectProperty<>(RepeatMode.ALL);
    private final StringProperty albumFilter = new SimpleStringProperty("");

    /**
     * Creates the view model.
     *
     * @param windowManager window manager used to open other windows
     * @param appModel application-wide state of the music library
     * @param musicPlayer player used to play audio files
     * @param folderOpener opener that shows a folder in the file manager
     * @throws NullPointerException if windowManager, appModel, musicPlayer, or
     *             folderOpener is null
     */
    public LibraryManagerViewModel(WindowManager windowManager, MusicLibraryAppModel appModel,
        MusicPlayer musicPlayer, FolderOpener folderOpener) {
        this.windowManager =
            Objects.requireNonNull(windowManager, "windowManager must not be null");
        this.appModel = Objects.requireNonNull(appModel, "appModel must not be null");
        this.musicPlayer = Objects.requireNonNull(musicPlayer, "musicPlayer must not be null");
        this.folderOpener = Objects.requireNonNull(folderOpener, "folderOpener must not be null");
        musicPlayer.setOnFinished(this::continuePlaybackWhenTrackFinishes);
        appModel.getFiles().subscribe(this::updateAlbums);
        // subscribe(Consumer) also fires with the current value, which
        // computes the initial album list here.
        sortKey.subscribe(_ -> updateAlbums());
        albumFilter.subscribe(_ -> updateAlbums());
    }

    private void continuePlaybackWhenTrackFinishes() {
        if (playerState.get() != PlayerState.PLAYING) { return; }
        if (repeatMode.get() == RepeatMode.ONE) {
            musicPlayer.play(selectedTrack.get().path());
        } else {
            nextTrack();
        }
    }

    private void updateAlbums() {
        albums.setAll(
            new AlbumDetector(appModel.getFiles()).detect().stream()
                .filter(this::matchesFilter)
                .sorted(albumComparator())
                .toList()
        );
    }

    /**
     * Returns the filter text of the album list. The albums are narrowed to
     * the ones whose name or artist contains the text, ignoring case,
     * whenever the text changes.
     *
     * @return string property of the filter text
     */
    public StringProperty albumFilterProperty() {
        return albumFilter;
    }

    /**
     * Clears the filter text of the album list, which restores the full album
     * list.
     */
    public void clearAlbumFilter() {
        albumFilter.set("");
    }

    private boolean matchesFilter(Album album) {
        return containsIgnoringCase(album.name(), albumFilter.get())
            || containsIgnoringCase(album.artist(), albumFilter.get());
    }

    private static boolean containsIgnoringCase(String text, String query) {
        return text.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }

    private Comparator<Album> albumComparator() {
        return switch (sortKey.get()) {
            case ALBUM -> Comparator.comparing(Album::name, String.CASE_INSENSITIVE_ORDER);
            case ARTIST -> Comparator.comparing(Album::artist, String.CASE_INSENSITIVE_ORDER);
        };
    }

    /**
     * Returns the sort key of the album list. The albums are re-published in
     * ascending order of the key, ignoring case, whenever the key changes.
     *
     * @return object property of the sort key
     */
    public ObjectProperty<SortKey> sortKeyProperty() {
        return sortKey;
    }

    /**
     * Returns the text of an album row in the album list. The parts are
     * ordered by the sort key: "album - artist" for the album key, and
     * "artist - album" for the artist key.
     *
     * @param album album shown in the row
     * @return text of the album row
     * @throws NullPointerException if album is null
     */
    public String albumRowText(Album album) {
        Objects.requireNonNull(album, "album must not be null");
        return switch (sortKey.get()) {
            case ALBUM -> album.name() + " - " + album.artist();
            case ARTIST -> album.artist() + " - " + album.name();
        };
    }

    /**
     * Returns the text of a track row in the track list: the track number,
     * the title, and the artist in parentheses. A track with an empty title
     * is shown by its file name. An empty track number is omitted. The artist
     * is shown only when it differs from the artist of the selected album.
     *
     * @param file track shown in the row
     * @return text of the track row
     * @throws NullPointerException if file is null
     */
    public String trackRowText(MusicFile file) {
        Objects.requireNonNull(file, "file must not be null");
        var title = file.tag().title().isEmpty()
            ? file.path().getFileName().toString()
            : file.tag().title();
        var number = file.tag().trackNumber();
        var text = number.isEmpty() ? title : number + ". " + title;
        var artist = file.tag().artist();
        if (artist.isEmpty() || artist.equals(selectedAlbumArtist())) { return text; }
        return text + " (" + artist + ")";
    }

    private String selectedAlbumArtist() {
        var album = appModel.selectedAlbumProperty().get();
        return album == null ? "" : album.artist();
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
     * Opens the album metadata edit window.
     */
    public void openAlbumEditWindow() {
        windowManager.showWindow(AlbumEditView.class, StageBuilder.create().build());
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
     * {@link #getSelectedTracks()} in track number order, and the first track
     * becomes the selected track. While a track is playing, the selected track
     * is kept instead of moving to the first track.
     *
     * @param album album to select, or null to clear the selection
     */
    public void selectAlbum(Album album) {
        appModel.selectAlbum(album);
        selectedTracks.setAll(album == null ? List.of() : orderByTrackNumber(album.files()));
        if (playerState.get() == PlayerState.PLAYING) { return; }
        selectedTrack.set(selectedTracks.isEmpty() ? null : selectedTracks.getFirst());
    }

    /**
     * Returns the selected album shared through the app model.
     *
     * @return read-only property of the selected album, holding null when no
     *     album is selected
     */
    public ReadOnlyObjectProperty<Album> selectedAlbumProperty() {
        return appModel.selectedAlbumProperty();
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
        }
    }

    /**
     * Plays the given track from the beginning, stopping the current playback.
     * The track becomes the selected track.
     *
     * @param track track to play
     * @throws NullPointerException if track is null
     */
    public void playTrack(MusicFile track) {
        Objects.requireNonNull(track, "track must not be null");
        selectedTrack.set(track);
        musicPlayer.play(track.path());
        playerState.set(PlayerState.PLAYING);
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
                var selected = selectedTrack.get();
                if (selected == null
                    || musicPlayer.playingPath().equals(Optional.of(selected.path()))) {
                    musicPlayer.resume();
                } else {
                    // The selection moved while paused; the selected track
                    // starts from the beginning.
                    musicPlayer.play(selected.path());
                }
                playerState.set(PlayerState.PLAYING);
            }
            case STOPPED -> {
                if (selectedTrack.get() != null) {
                    musicPlayer.play(selectedTrack.get().path());
                    playerState.set(PlayerState.PLAYING);
                }
            }
        }
    }

    /**
     * Opens the folder of the selected track in the file manager. Does nothing
     * when no track is selected.
     */
    public void openTrackFolder() {
        var selected = selectedTrack.get();
        if (selected == null) { return; }
        folderOpener.open(selected.path().getParent());
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
     * Returns the repeat mode of the playback.
     *
     * @return read-only property of the repeat mode
     */
    public ReadOnlyObjectProperty<RepeatMode> repeatModeProperty() {
        return repeatMode;
    }

    /**
     * Toggles the repeat mode between {@link RepeatMode#ALL} and
     * {@link RepeatMode#ONE}.
     */
    public void toggleRepeatMode() {
        repeatMode.set(repeatMode.get() == RepeatMode.ALL ? RepeatMode.ONE : RepeatMode.ALL);
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
