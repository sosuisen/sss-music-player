package com.sosuisha.presentation.screens.librarymanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sosuisha.domain.model.Album;
import com.sosuisha.domain.model.Favorite;
import com.sosuisha.domain.model.FavoriteAlbum;
import com.sosuisha.domain.model.FavoriteTrack;
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

class LibraryManagerFavoritesViewModelTest {
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

    @Test
    @DisplayName("ソートキーにFavoritesを選ぶと、左のリストはお気に入りのアルバムと曲を登録順に並べる")
    void choosing_favorites_lists_the_favorite_albums_and_tracks_in_the_order_they_were_added() {
        var viewModel = createViewModel(
            List.of(new FavoriteTrack(SONG.path()), new FavoriteAlbum("Album A", "X"))
        );

        viewModel.sortKeyProperty().set(SortKey.FAVORITES);

        assertEquals(
            List.of(new TrackItem(SONG), new AlbumItem(ALBUM_A)), viewModel.getListItems()
        );
    }

    @Test
    @DisplayName("ライブラリに見つからないお気に入りは、左のリストに表示しない")
    void favorites_not_found_in_the_library_are_not_listed() {
        var viewModel = createViewModel(
            List.of(
                new FavoriteTrack(Path.of("gone", "song.mp3")),
                new FavoriteAlbum("Gone Album", "Z"),
                new FavoriteTrack(SONG.path())
            )
        );

        viewModel.sortKeyProperty().set(SortKey.FAVORITES);

        assertEquals(List.of(new TrackItem(SONG)), viewModel.getListItems());
    }

    @Test
    @DisplayName("Favorites表示中にお気に入りを解除しても、行は未登録の状態で残る")
    void a_row_unregistered_while_favorites_are_shown_stays_as_unregistered() {
        var viewModel = createViewModel(List.of(new FavoriteAlbum("Album A", "X")));
        viewModel.sortKeyProperty().set(SortKey.FAVORITES);

        viewModel.toggleFavorite(new AlbumItem(ALBUM_A));

        assertEquals(List.of(new AlbumItem(ALBUM_A)), viewModel.getListItems());
        assertFalse(viewModel.isFavorite(new AlbumItem(ALBUM_A)));
    }

    @Test
    @DisplayName("解除した行は、Favoritesを選び直すと左のリストから消える")
    void an_unregistered_row_disappears_when_favorites_is_chosen_again() {
        var viewModel = createViewModel(
            List.of(new FavoriteAlbum("Album A", "X"), new FavoriteTrack(SONG.path()))
        );
        viewModel.sortKeyProperty().set(SortKey.FAVORITES);
        viewModel.toggleFavorite(new AlbumItem(ALBUM_A));

        viewModel.sortKeyProperty().set(SortKey.ALBUM);
        viewModel.sortKeyProperty().set(SortKey.FAVORITES);

        assertEquals(List.of(new TrackItem(SONG)), viewModel.getListItems());
    }

    @Test
    @DisplayName("曲の項目を選ぶと、曲リストはその曲だけになり、選択中のアルバムはその曲のアルバムになる")
    void selecting_a_track_row_shows_only_the_track_and_selects_its_album() {
        var viewModel = createViewModel(List.of(new FavoriteTrack(TWO.path())));
        viewModel.sortKeyProperty().set(SortKey.FAVORITES);

        viewModel.selectListItem(new TrackItem(TWO));

        assertEquals(List.of(TWO), viewModel.getSelectedTracks());
        assertEquals(TWO, viewModel.selectedTrackProperty().get());
        assertEquals(ALBUM_A, viewModel.selectedAlbumProperty().get());
    }

    @Test
    @DisplayName("曲の項目の行は曲名で表示し、曲名が空ならファイル名で表示する")
    void a_track_row_shows_the_title_or_the_file_name_when_the_title_is_empty() {
        var viewModel = createViewModel(List.of());
        var untitled = new MusicFile(Path.of("c", "untitled.mp3"), 400);

        assertEquals("Song", viewModel.listItemText(new TrackItem(SONG)));
        assertEquals("untitled.mp3", viewModel.listItemText(new TrackItem(untitled)));
    }

    @Test
    @DisplayName("Favorites表示でも絞り込みが効き、曲は曲名かアーティストで絞り込まれる")
    void the_filter_narrows_the_favorites_and_matches_tracks_by_title_or_artist() {
        var viewModel = createViewModel(
            List.of(new FavoriteAlbum("Album A", "X"), new FavoriteTrack(SONG.path()))
        );
        viewModel.sortKeyProperty().set(SortKey.FAVORITES);

        viewModel.albumFilterProperty().set("song");
        var byTitle = List.copyOf(viewModel.getListItems());
        viewModel.albumFilterProperty().set("y");
        var byArtist = List.copyOf(viewModel.getListItems());
        viewModel.albumFilterProperty().set("album a");
        var byAlbumName = List.copyOf(viewModel.getListItems());

        assertEquals(List.of(new TrackItem(SONG)), byTitle);
        assertEquals(List.of(new TrackItem(SONG)), byArtist);
        assertEquals(List.of(new AlbumItem(ALBUM_A)), byAlbumName);
    }

    @Test
    @DisplayName("Favorites表示の絞り込みは、曲名が一致する曲を含むアルバムの項目も残す")
    void the_filter_of_the_favorites_keeps_album_rows_with_a_matching_track_title() {
        var viewModel = createViewModel(List.of(new FavoriteAlbum("Album A", "X")));
        viewModel.sortKeyProperty().set(SortKey.FAVORITES);

        viewModel.albumFilterProperty().set("two");

        assertEquals(List.of(new AlbumItem(ALBUM_A)), viewModel.getListItems());
    }

    private static LibraryManagerViewModel createViewModel(List<Favorite> favorites) {
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()), new SimpleObjectProperty<>()
        );
        appModel.setFiles(List.of(ONE, TWO, SONG));
        return new LibraryManagerViewModel(
            new WindowManager(),
            appModel,
            new SettingsAppModel(new NullSettingsRepository()),
            new FavoritesAppModel(new InMemoryFavoriteRepository(favorites)),
            new NullMusicPlayer(),
            _ -> {
            }
        );
    }
}
