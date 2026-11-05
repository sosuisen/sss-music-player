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
