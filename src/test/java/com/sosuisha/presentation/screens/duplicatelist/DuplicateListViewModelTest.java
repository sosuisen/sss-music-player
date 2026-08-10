package com.sosuisha.presentation.screens.duplicatelist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.service.NullMusicPlayer;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.service.LibraryScanner;
import com.sosuisha.service.SettingsRepository;

import javafx.collections.ObservableList;

class DuplicateListViewModelTest {
    @Test
    @DisplayName("detectで重複判定関数を呼び出して、重複リストをObservableListに格納する")
    void detect_stores_duplicated_items_in_observable_list() {
        var items = List.of(
            new DuplicatedItems(
                "first.mp3",
                List.of(
                    new MusicFile(Path.of("a/first.mp3"), 100),
                    new MusicFile(Path.of("b/first.mp3"), 100)
                )
            )
        );
        var viewModel = new DuplicateListViewModel(
            new MusicLibraryAppModel(
                new LibraryScanner(), new SettingsAppModel(new SettingsRepository())
            ),
            new NullMusicPlayer()
        );

        viewModel.detect(() -> items);

        assertInstanceOf(ObservableList.class, viewModel.getDuplicatedItems());
        assertEquals(items, viewModel.getDuplicatedItems());
    }

    @Test
    @DisplayName("detectを呼ぶと、チェック状態はすべてクリアされる")
    void detect_clears_all_checked_states() {
        var item = new DuplicatedItems(
            "first.mp3",
            List.of(
                new MusicFile(Path.of("a/first.mp3"), 100),
                new MusicFile(Path.of("b/first.mp3"), 100)
            )
        );
        var viewModel = new DuplicateListViewModel(
            new MusicLibraryAppModel(
                new LibraryScanner(), new SettingsAppModel(new SettingsRepository())
            ),
            new NullMusicPlayer()
        );
        viewModel.detect(() -> List.of(item));
        viewModel.checkedProperty(item).set(true);

        viewModel.detect(() -> List.of(item));

        assertFalse(viewModel.checkedProperty(item).get());
    }
}
