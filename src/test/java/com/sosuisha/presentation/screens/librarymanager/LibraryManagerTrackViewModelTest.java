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
import com.sosuisha.domain.model.MusicFile;
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
 * The album list while {@link SortKey#TRACK} is chosen, and the search by
 * track title in the other views.
 */
class LibraryManagerTrackViewModelTest {
    private static final MusicFile CHERRY = new MusicFile(
        Path.of("a", "cherry.mp3"), 100, new TrackMetadata("Cherry", "X", "Album A", "X", "1", "")
    );
    private static final MusicFile APPLE = new MusicFile(
        Path.of("a", "apple.mp3"), 200, new TrackMetadata("apple", "X", "Album A", "X", "2", "")
    );
    private static final MusicFile UNTITLED = new MusicFile(Path.of("b", "banana.mp3"), 300);
    private static final MusicFile DATE = new MusicFile(
        Path.of("c", "date.mp3"), 400, new TrackMetadata("Date", "Zed", "Album C", "Zed", "1", "")
    );

    private final AtomicReference<@Nullable Runnable> onFinished = new AtomicReference<>();
    private final List<Path> playedPaths = new ArrayList<>();

    @Test
    @DisplayName("Trackを選ぶと、左のリストは全曲を曲名の昇順（大文字小文字無視）で並べ、曲名が空の曲はファイル名で並べる")
    void choosing_track_lists_all_tracks_by_title_ignoring_case_or_by_file_name_without_a_title() {
        var viewModel = createViewModel(List.of(CHERRY, APPLE, UNTITLED));

        viewModel.sortKeyProperty().set(SortKey.TRACK);

        assertEquals(
            List.of(new TrackItem(APPLE), new TrackItem(UNTITLED), new TrackItem(CHERRY)),
            viewModel.getListItems()
        );
    }

    @Test
    @DisplayName("Track表示の行は「曲名 - アーティスト」で表示し、アーティストが空なら曲名だけを表示する")
    void a_row_of_the_track_view_shows_the_title_and_the_artist_or_only_the_title_without_an_artist() {
        var viewModel = createViewModel(List.of(CHERRY, UNTITLED));

        viewModel.sortKeyProperty().set(SortKey.TRACK);

        assertEquals("Cherry - X", viewModel.listItemText(new TrackItem(CHERRY)));
        assertEquals("banana.mp3", viewModel.listItemText(new TrackItem(UNTITLED)));
    }

    @Test
    @DisplayName("Track表示の絞り込みは、曲名かアーティストの部分一致で、大文字小文字を無視する")
    void the_filter_of_the_track_view_matches_the_title_or_the_artist_ignoring_case() {
        var viewModel = createViewModel(List.of(CHERRY, APPLE, DATE));
        viewModel.sortKeyProperty().set(SortKey.TRACK);

        viewModel.albumFilterProperty().set("CHER");
        var byTitle = List.copyOf(viewModel.getListItems());
        viewModel.albumFilterProperty().set("zed");
        var byArtist = List.copyOf(viewModel.getListItems());

        assertEquals(List.of(new TrackItem(CHERRY)), byTitle);
        assertEquals(List.of(new TrackItem(DATE)), byArtist);
    }

    @Test
    @DisplayName("Track表示では、曲が終わると次の行を選び、その曲を再生する")
    void in_the_track_view_when_a_track_ends_the_next_row_is_selected_and_its_track_plays() {
        var viewModel = createViewModel(List.of(APPLE, CHERRY));
        viewModel.sortKeyProperty().set(SortKey.TRACK);
        viewModel.playListItem(new TrackItem(APPLE));

        finishTrack();

        assertEquals(new TrackItem(CHERRY), viewModel.selectedListItemProperty().get());
        assertEquals(List.of(CHERRY), viewModel.getSelectedTracks());
        assertEquals(CHERRY.path(), playedPaths.getLast());
    }

    @Test
    @DisplayName("Track表示では、最後の行の曲が終わると、先頭の行の曲を再生する")
    void in_the_track_view_when_the_track_of_the_last_row_ends_the_first_row_plays() {
        var viewModel = createViewModel(List.of(APPLE, CHERRY));
        viewModel.sortKeyProperty().set(SortKey.TRACK);
        viewModel.playListItem(new TrackItem(CHERRY));

        finishTrack();

        assertEquals(new TrackItem(APPLE), viewModel.selectedListItemProperty().get());
        assertEquals(APPLE.path(), playedPaths.getLast());
    }

    @Test
    @DisplayName("Track表示では、前の曲へ移ると、前の行を選び、その曲を再生する")
    void in_the_track_view_moving_to_the_previous_track_plays_the_previous_row() {
        var viewModel = createViewModel(List.of(APPLE, CHERRY));
        viewModel.sortKeyProperty().set(SortKey.TRACK);
        viewModel.playListItem(new TrackItem(CHERRY));

        viewModel.previousTrack();

        assertEquals(new TrackItem(APPLE), viewModel.selectedListItemProperty().get());
        assertEquals(APPLE.path(), playedPaths.getLast());
    }

    @Test
    @DisplayName("Album表示とArtist表示の絞り込みは、曲名が一致する曲を含むアルバムも残す")
    void the_filter_of_the_album_and_artist_views_keeps_albums_with_a_matching_track_title() {
        var viewModel = createViewModel(List.of(CHERRY, APPLE, DATE));
        var albumA = new AlbumItem(new Album("Album A", "X", List.of(CHERRY, APPLE)));

        viewModel.albumFilterProperty().set("cher");
        var inAlbumView = List.copyOf(viewModel.getListItems());
        viewModel.sortKeyProperty().set(SortKey.ARTIST);
        var inArtistView = List.copyOf(viewModel.getListItems());

        assertEquals(List.of(albumA), inAlbumView);
        assertEquals(List.of(albumA), inArtistView);
    }

    private void finishTrack() {
        Objects.requireNonNull(onFinished.get(), "no track finished callback").run();
    }

    private LibraryManagerViewModel createViewModel(List<MusicFile> files) {
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()), new SimpleObjectProperty<>()
        );
        appModel.setFiles(files);
        return new LibraryManagerViewModel(
            new WindowManager(),
            appModel,
            new SettingsAppModel(new NullSettingsRepository()),
            new FavoritesAppModel(new InMemoryFavoriteRepository(List.of())),
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
