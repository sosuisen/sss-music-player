package com.sosuisha.presentation.screens.duplicatelist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.TrackMetadata;
import com.sosuisha.domain.service.NullLibraryRepository;
import com.sosuisha.domain.service.NullMusicPlayer;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.service.DuplicateFileMover;
import com.sosuisha.service.LibraryIndexer;
import com.sosuisha.repository.SettingsRepositoryImpl;

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
                new LibraryIndexer(new NullLibraryRepository()),
                new SettingsAppModel(new SettingsRepositoryImpl())
            ),
            new NullMusicPlayer(),
            new DuplicateFileMover(Path.of("duplicates"), Path.of("duplicates.log")),
            _ -> {
            }
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
                new LibraryIndexer(new NullLibraryRepository()),
                new SettingsAppModel(new SettingsRepositoryImpl())
            ),
            new NullMusicPlayer(),
            new DuplicateFileMover(Path.of("duplicates"), Path.of("duplicates.log")),
            _ -> {
            }
        );
        viewModel.detect(() -> List.of(item));
        viewModel.checkedProperty(item).set(true);

        viewModel.detect(() -> List.of(item));

        assertFalse(viewModel.checkedProperty(item).get());
    }

    @Test
    @DisplayName("チェックされたグループだけが、重複除去の移動に渡される")
    void only_checked_groups_are_passed_to_the_duplicate_file_mover() {
        var checked = new DuplicatedItems(
            "first.mp3",
            List.of(
                new MusicFile(Path.of("a/first.mp3"), 100),
                new MusicFile(Path.of("b/first.mp3"), 100)
            )
        );
        var unchecked = new DuplicatedItems(
            "second.m4a",
            List.of(
                new MusicFile(Path.of("c/second.m4a"), 200),
                new MusicFile(Path.of("d/second.m4a"), 200)
            )
        );
        // AtomicReference is a mutable box to capture the argument of the
        // overridden method.
        var movedGroups = new AtomicReference<List<DuplicatedItems>>();
        var mover = new DuplicateFileMover(Path.of("duplicates"), Path.of("duplicates.log")) {
            @Override
            public void moveDuplicates(List<DuplicatedItems> groups) {
                movedGroups.set(groups);
            }
        };
        var viewModel = new DuplicateListViewModel(
            new MusicLibraryAppModel(
                new LibraryIndexer(new NullLibraryRepository()),
                new SettingsAppModel(new SettingsRepositoryImpl())
            ),
            new NullMusicPlayer(),
            mover,
            _ -> {
            }
        );
        viewModel.detect(() -> List.of(checked, unchecked));
        viewModel.checkedProperty(checked).set(true);

        viewModel.removeCheckedDuplicates();

        assertEquals(List.of(checked), movedGroups.get());
    }

    @Test
    @DisplayName("detectByMetadataは、曲名とアーティストの一致で重複を判定して格納する")
    void detect_by_metadata_stores_duplicates_detected_by_title_and_artist() {
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()),
            new SettingsAppModel(new SettingsRepositoryImpl())
        );
        var viewModel = new DuplicateListViewModel(
            appModel,
            new NullMusicPlayer(),
            new DuplicateFileMover(Path.of("duplicates"), Path.of("duplicates.log")),
            _ -> {
            }
        );
        var sameA = new MusicFile(Path.of("a/one.mp3"), 100, tag("Song", "Artist"));
        var sameB = new MusicFile(Path.of("b/two.mp3"), 100, tag("Song", "Artist"));
        var differentArtist = new MusicFile(Path.of("c/three.mp3"), 300, tag("Song", "Other"));
        appModel.setFiles(List.of(sameA, sameB, differentArtist));

        viewModel.detectByMetadata();

        assertEquals(
            List.of(new DuplicatedItems("Song - Artist", List.of(sameA, sameB))),
            viewModel.getDuplicatedItems()
        );
    }

    private static TrackMetadata tag(String title, String artist) {
        return new TrackMetadata(title, artist, "", "", "", "");
    }

    @Test
    @DisplayName("ライブラリのファイル一覧が変わると、最後の判定条件で候補リストが更新される")
    void the_candidate_list_is_updated_with_the_last_detector_when_the_library_files_change() {
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()),
            new SettingsAppModel(new SettingsRepositoryImpl())
        );
        var viewModel = new DuplicateListViewModel(
            appModel,
            new NullMusicPlayer(),
            new DuplicateFileMover(Path.of("duplicates"), Path.of("duplicates.log")),
            _ -> {
            }
        );
        appModel.setFiles(
            List.of(
                new MusicFile(Path.of("a/dup.mp3"), 100),
                new MusicFile(Path.of("b/dup.mp3"), 100)
            )
        );
        viewModel.detectByFilename();

        appModel.setFiles(List.of(new MusicFile(Path.of("a/dup.mp3"), 100)));

        assertEquals(List.of(), viewModel.getDuplicatedItems());
    }

    @Test
    @DisplayName("重複除去を実行すると、ライブラリが再スキャンされる")
    void removing_checked_duplicates_rescans_the_library() {
        var rescanned = new AtomicBoolean(false);
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()),
            new SettingsAppModel(new SettingsRepositoryImpl())
        ) {
            @Override
            public void rescan() {
                rescanned.set(true);
            }
        };
        var mover = new DuplicateFileMover(Path.of("duplicates"), Path.of("duplicates.log")) {
            @Override
            public void moveDuplicates(List<DuplicatedItems> groups) {}
        };
        var viewModel =
            new DuplicateListViewModel(appModel, new NullMusicPlayer(), mover, _ -> {
            });

        viewModel.removeCheckedDuplicates();

        assertTrue(rescanned.get());
    }
}
