package com.sosuisha.presentation.screens.librarymanager;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import com.sosuisha.domain.exception.FolderOpenException;
import com.sosuisha.domain.model.Album;
import com.sosuisha.domain.model.Favorite;
import com.sosuisha.domain.model.FavoriteAlbum;
import com.sosuisha.domain.model.FavoriteTrack;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.PlaybackPosition;
import com.sosuisha.domain.model.RepeatMode;
import com.sosuisha.domain.service.FolderOpener;
import com.sosuisha.domain.service.MusicPlayer;
import com.sosuisha.presentation.WindowManager;
import com.sosuisha.presentation.appmodel.FavoritesAppModel;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.presentation.screens.albumedit.AlbumEditView;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListView;
import com.sosuisha.presentation.screens.librarymanager.AlbumListItem.AlbumItem;
import com.sosuisha.presentation.screens.librarymanager.AlbumListItem.TrackItem;
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
import org.jspecify.annotations.Nullable;

/**
 * ViewModel for the library manager screen.
 */
public class LibraryManagerViewModel {
    private final WindowManager windowManager;
    private final MusicLibraryAppModel appModel;
    private final SettingsAppModel settingsAppModel;
    private final FavoritesAppModel favoritesAppModel;
    private final MusicPlayer musicPlayer;
    private final FolderOpener folderOpener;
    private final ObservableList<AlbumListItem> listItems = FXCollections.observableArrayList();
    private final ObservableList<MusicFile> selectedTracks = FXCollections.observableArrayList();
    private final ObjectProperty<@Nullable AlbumListItem> selectedListItem =
        new SimpleObjectProperty<>();
    private final ObjectProperty<PlayerState> playerState =
        new SimpleObjectProperty<>(PlayerState.STOPPED);
    private final ObjectProperty<MusicFile> selectedTrack = new SimpleObjectProperty<>();
    private final ObjectProperty<SortKey> sortKey = new SimpleObjectProperty<>(SortKey.ALBUM);
    private final StringProperty albumFilter = new SimpleStringProperty("");
    private final StringProperty errorMessage = new SimpleStringProperty();
    // Null while the position is not known, such as while stopped.
    private final ObjectProperty<@Nullable PlaybackPosition> playbackPosition =
        new SimpleObjectProperty<>();

    /**
     * Creates the view model.
     *
     * @param windowManager window manager used to open other windows
     * @param appModel application-wide state of the music library
     * @param settingsAppModel application-wide state of the settings
     * @param favoritesAppModel application-wide state of the favorites
     * @param musicPlayer player used to play audio files
     * @param folderOpener opener that shows a folder in the file manager
     * @throws NullPointerException if windowManager, appModel, settingsAppModel,
     *             favoritesAppModel, musicPlayer, or folderOpener is null
     */
    public LibraryManagerViewModel(WindowManager windowManager, MusicLibraryAppModel appModel,
        SettingsAppModel settingsAppModel, FavoritesAppModel favoritesAppModel,
        MusicPlayer musicPlayer, FolderOpener folderOpener) {
        this.windowManager =
            Objects.requireNonNull(windowManager, "windowManager must not be null");
        this.appModel = Objects.requireNonNull(appModel, "appModel must not be null");
        this.settingsAppModel =
            Objects.requireNonNull(settingsAppModel, "settingsAppModel must not be null");
        this.favoritesAppModel =
            Objects.requireNonNull(favoritesAppModel, "favoritesAppModel must not be null");
        this.musicPlayer = Objects.requireNonNull(musicPlayer, "musicPlayer must not be null");
        this.folderOpener = Objects.requireNonNull(folderOpener, "folderOpener must not be null");
        musicPlayer.setOnFinished(this::continuePlaybackWhenTrackFinishes);
        musicPlayer.setOnError(this::stopWhenPlaybackFails);
        musicPlayer.setOnProgress(playbackPosition::set);
        appModel.getFiles().subscribe(this::updateAlbums);
        // subscribe(Consumer) also fires with the current value, which
        // computes the initial album list here.
        sortKey.subscribe(_ -> updateAlbums());
        albumFilter.subscribe(_ -> updateAlbums());
    }

    private void stopWhenPlaybackFails(String reason) {
        var track = selectedTrack.get();
        playerState.set(PlayerState.STOPPED);
        playbackPosition.set(null);
        errorMessage.set("Cannot play " + track.path().getFileName() + ": " + reason);
    }

    private void continuePlaybackWhenTrackFinishes() {
        if (playerState.get() != PlayerState.PLAYING) { return; }
        if (settingsAppModel.repeatModeProperty().get() == RepeatMode.ONE) {
            musicPlayer.play(selectedTrack.get().path());
        } else {
            nextTrack();
        }
    }

    private void updateAlbums() {
        if (sortKey.get() == SortKey.TRACK) {
            listItems.setAll(
                appModel.getFiles().stream()
                    .sorted(
                        Comparator.comparing(
                            LibraryManagerViewModel::titleOf, String.CASE_INSENSITIVE_ORDER
                        )
                    )
                    .<AlbumListItem>map(TrackItem::new)
                    .filter(this::matchesFilter)
                    .toList()
            );
            return;
        }
        var albums = new AlbumDetector(appModel.getFiles()).detect();
        if (sortKey.get() == SortKey.FAVORITES) {
            listItems.setAll(
                favoritesAppModel.getFavorites().stream()
                    .map(favorite -> toListItem(favorite, albums))
                    .flatMap(Optional::stream)
                    .filter(this::matchesFilter)
                    .toList()
            );
            return;
        }
        listItems.setAll(
            albums.stream()
                .filter(this::matchesFilter)
                .sorted(albumComparator())
                .<AlbumListItem>map(AlbumItem::new)
                .toList()
        );
    }

    private Optional<AlbumListItem> toListItem(Favorite favorite, List<Album> albums) {
        return switch (favorite) {
            case FavoriteAlbum(String name, String artist) -> albums.stream()
                .filter(album -> album.name().equals(name) && album.artist().equals(artist))
                .findFirst()
                .map(AlbumItem::new);
            case FavoriteTrack(Path path) -> appModel.getFiles().stream()
                .filter(file -> file.path().equals(path))
                .findFirst()
                .map(TrackItem::new);
        };
    }

    /**
     * Returns the filter text of the album list. The albums are narrowed to
     * the ones whose name, artist, or track title contains the text, ignoring
     * case, whenever the text changes. Among the favorites and in the track
     * view, the tracks are narrowed in the same way by their title or artist.
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

    private boolean matchesFilter(AlbumListItem item) {
        return switch (item) {
            case AlbumItem(Album album) -> matchesFilter(album);
            case TrackItem(MusicFile track) -> containsIgnoringCase(
                titleOf(track), albumFilter.get()
            )
                || containsIgnoringCase(track.tag().artist(), albumFilter.get());
        };
    }

    private boolean matchesFilter(Album album) {
        return containsIgnoringCase(album.name(), albumFilter.get())
            || containsIgnoringCase(album.artist(), albumFilter.get())
            || album.files().stream()
                .anyMatch(track -> containsIgnoringCase(titleOf(track), albumFilter.get()));
    }

    private static boolean containsIgnoringCase(String text, String query) {
        return text.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }

    private Comparator<Album> albumComparator() {
        return switch (sortKey.get()) {
            case ALBUM, TRACK, FAVORITES -> Comparator
                .comparing(Album::name, String.CASE_INSENSITIVE_ORDER);
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
            case ALBUM, TRACK, FAVORITES -> album.name() + " - " + album.artist();
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
        var title = titleOf(file);
        var number = file.tag().trackNumber();
        var text = number.isEmpty() ? title : number + ". " + title;
        var artist = file.tag().artist();
        if (artist.isEmpty() || artist.equals(selectedAlbumArtist())) { return text; }
        return text + " (" + artist + ")";
    }

    private static String titleOf(MusicFile file) {
        return file.tag().title().isEmpty()
            ? file.path().getFileName().toString()
            : file.tag().title();
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
     * Returns the rows of the album list. For the album and artist sort keys,
     * the rows are the albums recognized in the music library. For
     * {@link SortKey#TRACK}, the rows are all tracks sorted by title, ignoring
     * case. For {@link SortKey#FAVORITES}, the rows are the favorite albums and
     * tracks found in the library, in the order they were added. The list is
     * updated
     * whenever the files of the library, the sort key, or the filter text
     * change.
     *
     * @return observable list of the rows
     */
    public ObservableList<AlbumListItem> getListItems() {
        return listItems;
    }

    /**
     * Selects the given row of the album list. An album row selects the album
     * as {@link #selectAlbum(Album)} does. A track row selects the album that
     * holds the track, but publishes only that track through
     * {@link #getSelectedTracks()}.
     *
     * @param item row to select, or null to clear the selection
     */
    public void selectListItem(@Nullable AlbumListItem item) {
        selectedListItem.set(item);
        switch (item) {
            case null -> selectAlbum(null);
            case AlbumItem(Album album) -> selectAlbum(album);
            case TrackItem(MusicFile track) -> showTracks(albumOf(track), List.of(track));
        }
    }

    /**
     * Returns the selected row of the album list.
     *
     * @return read-only property of the selected row, holding null when no
     *     row is selected
     */
    public ReadOnlyObjectProperty<@Nullable AlbumListItem> selectedListItemProperty() {
        return selectedListItem;
    }

    /**
     * Selects the given row of the album list as
     * {@link #selectListItem(AlbumListItem)} does, and plays the first of the
     * shown tracks from the beginning.
     *
     * @param item row to play
     * @throws NullPointerException if item is null
     */
    public void playListItem(AlbumListItem item) {
        Objects.requireNonNull(item, "item must not be null");
        selectListItem(item);
        playTrack(selectedTracks.getFirst());
    }

    private @Nullable Album albumOf(MusicFile track) {
        return new AlbumDetector(appModel.getFiles()).detect().stream()
            .filter(album -> album.files().contains(track))
            .findFirst()
            .orElse(null);
    }

    /**
     * Returns the text of a row in the album list. An album row is shown as
     * {@link #albumRowText(Album)} returns. A track row is shown by the title
     * of the track, or by its file name when the title is empty. While
     * {@link SortKey#TRACK} is chosen, the artist follows the title as
     * "title - artist", unless the artist is empty.
     *
     * @param item row of the album list
     * @return text of the row
     * @throws NullPointerException if item is null
     */
    public String listItemText(AlbumListItem item) {
        Objects.requireNonNull(item, "item must not be null");
        return switch (item) {
            case AlbumItem(Album album) -> albumRowText(album);
            case TrackItem(MusicFile track) -> trackListItemText(track);
        };
    }

    private String trackListItemText(MusicFile track) {
        var artist = track.tag().artist();
        if (sortKey.get() != SortKey.TRACK || artist.isEmpty()) { return titleOf(track); }
        return titleOf(track) + " - " + artist;
    }

    /**
     * Toggles the favorite of the album or the track shown in the given row.
     * The rows of the album list are not rebuilt, so a row unregistered
     * while the favorites are shown stays until the list is rebuilt.
     *
     * @param item row whose album or track is toggled
     * @throws NullPointerException if item is null
     */
    public void toggleFavorite(AlbumListItem item) {
        Objects.requireNonNull(item, "item must not be null");
        favoritesAppModel.toggle(toFavorite(item));
    }

    /**
     * Returns whether the album or the track shown in the given row is a
     * favorite.
     *
     * @param item row of the album list or the track list
     * @return true if the album or the track is a favorite
     * @throws NullPointerException if item is null
     */
    public boolean isFavorite(AlbumListItem item) {
        Objects.requireNonNull(item, "item must not be null");
        return favoritesAppModel.getFavorites().contains(toFavorite(item));
    }

    /**
     * Returns the favorites shared through the favorites app model.
     *
     * @return observable list of the favorites in the order they were added
     */
    public ObservableList<Favorite> getFavorites() {
        return favoritesAppModel.getFavorites();
    }

    private static Favorite toFavorite(AlbumListItem item) {
        return switch (item) {
            case AlbumItem(Album album) -> new FavoriteAlbum(album.name(), album.artist());
            case TrackItem(MusicFile track) -> new FavoriteTrack(track.path());
        };
    }

    /**
     * Selects the given album. The tracks of the album are published through
     * {@link #getSelectedTracks()} in track number order, and the first track
     * becomes the selected track. While a track is playing, the selected track
     * is kept instead of moving to the first track.
     *
     * @param album album to select, or null to clear the selection
     */
    public void selectAlbum(@Nullable Album album) {
        showTracks(album, album == null ? List.of() : orderByTrackNumber(album.files()));
    }

    private void showTracks(@Nullable Album album, List<MusicFile> tracks) {
        appModel.selectAlbum(album);
        selectedTracks.setAll(tracks);
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
     * Selects the given track as the playback target. While a track is
     * playing, clearing the selection is ignored so that the playing track
     * stays selected.
     *
     * @param track track to select, or null to clear the selection
     */
    public void selectTrack(@Nullable MusicFile track) {
        if (track == null && playerState.get() == PlayerState.PLAYING) { return; }
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
     * to the first track at the end. While the favorites or the tracks are
     * shown, the end of the selected row moves instead to the first track of
     * the next row,
     * and the last row wraps to the first row. When a track is playing, the
     * moved-to track is played immediately. Does nothing when the album has
     * no tracks.
     */
    public void nextTrack() {
        moveSelection(1);
    }

    /**
     * Moves the selection to the previous track of the selected album,
     * wrapping to the last track at the head. While the favorites or the
     * tracks are shown, the head of the selected row moves instead to the last
     * track of the
     * previous row, and the first row wraps to the last row. When a track is
     * playing, the moved-to track is played immediately. Does nothing when
     * the album has no tracks.
     */
    public void previousTrack() {
        moveSelection(-1);
    }

    private void moveSelection(int offset) {
        if (selectedTracks.isEmpty()) { return; }
        var index = selectedTracks.indexOf(selectedTrack.get());
        var size = selectedTracks.size();
        var movedIndex = index + offset;
        var leavesRow = index >= 0 && (movedIndex < 0 || movedIndex >= size);
        if (leavesRow && moveToAdjacentRow(offset)) { return; }
        moveSelectionTo(selectedTracks.get((movedIndex + size) % size));
    }

    // Among the favorites and the tracks, the playback goes on through the rows
    // of the list. Returns false when the selected row is not shown, so that
    // the caller wraps inside the row as for an album.
    private boolean moveToAdjacentRow(int offset) {
        var listIndex = listItems.indexOf(selectedListItem.get());
        var movesThroughRows =
            sortKey.get() == SortKey.FAVORITES || sortKey.get() == SortKey.TRACK;
        if (!movesThroughRows || listIndex < 0) { return false; }
        var listSize = listItems.size();
        selectListItem(listItems.get((listIndex + offset + listSize) % listSize));
        moveSelectionTo(offset > 0 ? selectedTracks.getFirst() : selectedTracks.getLast());
        return true;
    }

    private void moveSelectionTo(MusicFile track) {
        selectedTrack.set(track);
        if (playerState.get() == PlayerState.PLAYING) {
            startPlaying(track.path());
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
        startPlaying(track.path());
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
                    playerState.set(PlayerState.PLAYING);
                } else {
                    // The selection moved while paused; the selected track
                    // starts from the beginning.
                    startPlaying(selected.path());
                }
            }
            case STOPPED -> {
                if (selectedTrack.get() != null) {
                    startPlaying(selectedTrack.get().path());
                }
            }
        }
    }

    // The state becomes PLAYING before play(), because the player may report
    // a failure inside play() and the failure sets the state to STOPPED.
    private void startPlaying(Path path) {
        errorMessage.set(null);
        playbackPosition.set(null);
        playerState.set(PlayerState.PLAYING);
        musicPlayer.play(path);
    }

    /**
     * Returns the message of the last failed operation of this screen, or
     * null when the last operation succeeded.
     *
     * @return error message property
     */
    public StringProperty errorMessageProperty() {
        return errorMessage;
    }

    /**
     * Opens the folder of the selected track in the file manager. Does nothing
     * when no track is selected or the track has no parent folder. When the
     * folder cannot be opened, the error message is set to the error message
     * property.
     */
    public void openTrackFolder() {
        var selected = selectedTrack.get();
        if (selected == null) { return; }
        var folder = selected.path().getParent();
        if (folder == null) { return; }
        errorMessage.set(null);
        try {
            folderOpener.open(folder);
        } catch (FolderOpenException e) {
            errorMessage.set(e.getMessage());
        }
    }

    /**
     * Moves the playback of the playing or paused track to the given
     * position.
     *
     * @param position time from the start of the track
     * @throws NullPointerException if position is null
     */
    public void seek(Duration position) {
        Objects.requireNonNull(position, "position must not be null");
        musicPlayer.seek(position);
    }

    /**
     * Returns the position of the playback and the length of the playing
     * track.
     *
     * @return read-only property of the playback position, which holds null
     *     while the position is not known
     */
    public ReadOnlyObjectProperty<@Nullable PlaybackPosition> playbackPositionProperty() {
        return playbackPosition;
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
     * Returns the repeat mode of the playback shared through the settings app
     * model.
     *
     * @return read-only property of the repeat mode
     */
    public ReadOnlyObjectProperty<RepeatMode> repeatModeProperty() {
        return settingsAppModel.repeatModeProperty();
    }

    /**
     * Toggles the repeat mode of the settings app model between
     * {@link RepeatMode#ALL} and {@link RepeatMode#ONE}.
     */
    public void toggleRepeatMode() {
        var repeatMode = settingsAppModel.repeatModeProperty();
        repeatMode.set(repeatMode.get() == RepeatMode.ALL ? RepeatMode.ONE : RepeatMode.ALL);
    }

    /**
     * Stops the playback and moves the player state to
     * {@link PlayerState#STOPPED}.
     */
    public void stopPlayback() {
        musicPlayer.stop();
        playerState.set(PlayerState.STOPPED);
        playbackPosition.set(null);
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
