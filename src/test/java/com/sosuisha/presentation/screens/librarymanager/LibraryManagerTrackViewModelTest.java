package com.sosuisha.presentation.screens.librarymanager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
import com.sosuisha.presentation.screens.librarymanager.AlbumListItem.TrackItem;
import com.sosuisha.service.LibraryIndexer;

import javafx.beans.property.SimpleObjectProperty;

/**
 * The album list while {@link SortKey#TRACK} is chosen.
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

    private static LibraryManagerViewModel createViewModel(List<MusicFile> files) {
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()), new SimpleObjectProperty<>()
        );
        appModel.setFiles(files);
        return new LibraryManagerViewModel(
            new WindowManager(),
            appModel,
            new SettingsAppModel(new NullSettingsRepository()),
            new FavoritesAppModel(new InMemoryFavoriteRepository(List.of())),
            new NullMusicPlayer(),
            _ -> {
            }
        );
    }
}
