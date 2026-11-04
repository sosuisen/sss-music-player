package com.sosuisha.presentation.screens.librarymanager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sosuisha.domain.model.Album;
import com.sosuisha.domain.model.Favorite;
import com.sosuisha.domain.model.FavoriteAlbum;
import com.sosuisha.domain.model.FavoriteTrack;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.RepeatMode;
import com.sosuisha.domain.model.TrackMetadata;
import com.sosuisha.domain.repository.InMemoryFavoriteRepository;
import com.sosuisha.domain.repository.NullLibraryRepository;
import com.sosuisha.domain.repository.NullSettingsRepository;
import com.sosuisha.domain.service.NullMusicPlayer;
import com.sosuisha.presentation.WindowManager;
import com.sosuisha.presentation.appmodel.FavoritesAppModel;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.presentation.screens.librarymanager.AlbumListItem.AlbumItem;
import com.sosuisha.presentation.screens.librarymanager.AlbumListItem.TrackItem;
import com.sosuisha.service.LibraryIndexer;

import javafx.beans.property.SimpleObjectProperty;

/**
 * Continuous playback through the rows of the album list while the favorites
 * are shown.
 */
class LibraryManagerFavoritesPlaybackViewModelTest {
    private static final MusicFile ONE = new MusicFile(
        Path.of("a", "one.mp3"), 100, new TrackMetadata("One", "X", "Album A", "X", "1", "")
    );
    private static final MusicFile TWO = new MusicFile(
        Path.of("a", "two.mp3"), 200, new TrackMetadata("Two", "X", "Album A", "X", "2", "")
    );
    private static final MusicFile SONG = new MusicFile(
        Path.of("b", "song.mp3"), 300, new TrackMetadata("Song", "Y", "Album B", "Y", "1", "")
    );
    private static final Album ALBUM_A = new Album("Album A", "X", List.of(ONE, TWO));

    private final AtomicReference<@Nullable Runnable> onFinished = new AtomicReference<>();
    private final List<Path> playedPaths = new ArrayList<>();
    private final SettingsAppModel settingsAppModel =
        new SettingsAppModel(new NullSettingsRepository());

    @Test
    @DisplayName("アルバムの項目の最後の曲が終わると、次の項目を選び、その先頭の曲を再生する")
    void when_the_last_track_of_an_album_row_ends_the_next_row_is_selected_and_its_first_track_plays() {
        var viewModel = createViewModel(
            List.of(new FavoriteAlbum("Album A", "X"), new FavoriteTrack(SONG.path()))
        );
        viewModel.sortKeyProperty().set(SortKey.FAVORITES);
        viewModel.selectListItem(new AlbumItem(ALBUM_A));
        viewModel.playTrack(TWO);

        finishTrack();

        assertEquals(new TrackItem(SONG), viewModel.selectedListItemProperty().get());
        assertEquals(List.of(SONG), viewModel.getSelectedTracks());
        assertEquals(SONG, viewModel.selectedTrackProperty().get());
        assertEquals(SONG.path(), playedPaths.getLast());
    }

    @Test
    @DisplayName("停止中に項目の最後の曲で次の曲へ移ると、次の項目の先頭の曲を選ぶだけで再生しない")
    void moving_to_the_next_track_at_the_end_of_a_row_while_stopped_only_selects_the_next_row() {
        var viewModel = createViewModel(
            List.of(new FavoriteAlbum("Album A", "X"), new FavoriteTrack(SONG.path()))
        );
        viewModel.sortKeyProperty().set(SortKey.FAVORITES);
        viewModel.selectListItem(new AlbumItem(ALBUM_A));
        viewModel.selectTrack(TWO);

        viewModel.nextTrack();

        assertEquals(new TrackItem(SONG), viewModel.selectedListItemProperty().get());
        assertEquals(SONG, viewModel.selectedTrackProperty().get());
        assertEquals(List.of(), playedPaths);
    }

    @Test
    @DisplayName("項目の先頭の曲で前の曲へ移ると、前の項目を選び、その最後の曲を再生する")
    void moving_to_the_previous_track_at_the_head_of_a_row_plays_the_last_track_of_the_previous_row() {
        var viewModel = createViewModel(
            List.of(new FavoriteAlbum("Album A", "X"), new FavoriteTrack(SONG.path()))
        );
        viewModel.sortKeyProperty().set(SortKey.FAVORITES);
        viewModel.playListItem(new TrackItem(SONG));

        viewModel.previousTrack();

        assertEquals(new AlbumItem(ALBUM_A), viewModel.selectedListItemProperty().get());
        assertEquals(TWO, viewModel.selectedTrackProperty().get());
        assertEquals(TWO.path(), playedPaths.getLast());
    }

    @Test
    @DisplayName("アルバムの項目の途中の曲が終わると、その項目の次の曲を再生する")
    void when_a_middle_track_of_an_album_row_ends_the_next_track_of_the_row_plays() {
        var viewModel = createViewModel(
            List.of(new FavoriteAlbum("Album A", "X"), new FavoriteTrack(SONG.path()))
        );
        viewModel.sortKeyProperty().set(SortKey.FAVORITES);
        viewModel.playListItem(new AlbumItem(ALBUM_A));

        finishTrack();

        assertEquals(new AlbumItem(ALBUM_A), viewModel.selectedListItemProperty().get());
        assertEquals(TWO.path(), playedPaths.getLast());
    }

    @Test
    @DisplayName("曲の項目の曲が終わると、次の項目を選び、その先頭の曲を再生する")
    void when_the_track_of_a_track_row_ends_the_first_track_of_the_next_row_plays() {
        var viewModel = createViewModel(
            List.of(new FavoriteTrack(SONG.path()), new FavoriteAlbum("Album A", "X"))
        );
        viewModel.sortKeyProperty().set(SortKey.FAVORITES);
        viewModel.playListItem(new TrackItem(SONG));

        finishTrack();

        assertEquals(new AlbumItem(ALBUM_A), viewModel.selectedListItemProperty().get());
        assertEquals(ONE.path(), playedPaths.getLast());
    }

    @Test
    @DisplayName("最後の項目の最後の曲が終わると、先頭の項目の先頭の曲を再生する")
    void when_the_last_track_of_the_last_row_ends_the_first_track_of_the_first_row_plays() {
        var viewModel = createViewModel(
            List.of(new FavoriteAlbum("Album A", "X"), new FavoriteTrack(SONG.path()))
        );
        viewModel.sortKeyProperty().set(SortKey.FAVORITES);
        viewModel.playListItem(new TrackItem(SONG));

        finishTrack();

        assertEquals(new AlbumItem(ALBUM_A), viewModel.selectedListItemProperty().get());
        assertEquals(ONE.path(), playedPaths.getLast());
    }

    @Test
    @DisplayName("1曲リピートでは、項目の最後の曲が終わっても、同じ曲を再生し直す")
    void in_repeat_one_the_last_track_of_a_row_is_played_again() {
        var viewModel = createViewModel(
            List.of(new FavoriteAlbum("Album A", "X"), new FavoriteTrack(SONG.path()))
        );
        settingsAppModel.repeatModeProperty().set(RepeatMode.ONE);
        viewModel.sortKeyProperty().set(SortKey.FAVORITES);
        viewModel.selectListItem(new AlbumItem(ALBUM_A));
        viewModel.playTrack(TWO);

        finishTrack();

        assertEquals(new AlbumItem(ALBUM_A), viewModel.selectedListItemProperty().get());
        assertEquals(List.of(TWO.path(), TWO.path()), playedPaths);
    }

    @Test
    @DisplayName("先頭の項目の先頭の曲で前の曲へ移ると、最後の項目の最後の曲を再生する")
    void moving_to_the_previous_track_at_the_head_of_the_first_row_plays_the_last_row() {
        var viewModel = createViewModel(
            List.of(new FavoriteAlbum("Album A", "X"), new FavoriteTrack(SONG.path()))
        );
        viewModel.sortKeyProperty().set(SortKey.FAVORITES);
        viewModel.playListItem(new AlbumItem(ALBUM_A));

        viewModel.previousTrack();

        assertEquals(new TrackItem(SONG), viewModel.selectedListItemProperty().get());
        assertEquals(SONG.path(), playedPaths.getLast());
    }

    @Test
    @DisplayName("選択中の項目が絞り込みで左のリストから消えていると、項目の中で先頭の曲へ戻る")
    void when_the_selected_row_is_filtered_out_the_playback_wraps_inside_the_row() {
        var viewModel = createViewModel(
            List.of(new FavoriteAlbum("Album A", "X"), new FavoriteTrack(SONG.path()))
        );
        viewModel.sortKeyProperty().set(SortKey.FAVORITES);
        viewModel.selectListItem(new AlbumItem(ALBUM_A));
        viewModel.playTrack(TWO);
        viewModel.albumFilterProperty().set("song");

        finishTrack();

        assertEquals(ONE.path(), playedPaths.getLast());
    }

    @Test
    @DisplayName("Favorites表示でないときは、アルバムの最後の曲が終わると、そのアルバムの先頭の曲へ戻る")
    void outside_the_favorites_the_playback_wraps_inside_the_album() {
        var viewModel = createViewModel(
            List.of(new FavoriteAlbum("Album A", "X"), new FavoriteTrack(SONG.path()))
        );
        viewModel.selectListItem(new AlbumItem(ALBUM_A));
        viewModel.playTrack(TWO);

        finishTrack();

        assertEquals(new AlbumItem(ALBUM_A), viewModel.selectedListItemProperty().get());
        assertEquals(ONE.path(), playedPaths.getLast());
    }

    private void finishTrack() {
        Objects.requireNonNull(onFinished.get(), "no track finished callback").run();
    }

    private LibraryManagerViewModel createViewModel(List<Favorite> favorites) {
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()), new SimpleObjectProperty<>()
        );
        appModel.setFiles(List.of(ONE, TWO, SONG));
        return new LibraryManagerViewModel(
            new WindowManager(),
            appModel,
            settingsAppModel,
            new FavoritesAppModel(new InMemoryFavoriteRepository(favorites)),
            new NullMusicPlayer() {
                @Override
                public void play(Path path) {
                    playedPaths.add(path);
                }

                @Override
                public void setOnFinished(Runnable callback) {
                    onFinished.set(callback);
                }
            },
            _ -> {
            }
        );
    }
}
