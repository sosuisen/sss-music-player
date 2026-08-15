package com.sosuisha.presentation.appmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.Settings;
import com.sosuisha.domain.model.TrackMetadata;
import com.sosuisha.domain.service.LibraryRepository;
import com.sosuisha.domain.service.NullLibraryRepository;
import com.sosuisha.service.LibraryIndexer;
import com.sosuisha.domain.service.NullSettingsRepository;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class MusicLibraryAppModelTest {
    @TempDir
    Path folder;

    @Start
    void setup(Stage stage) {
        // Initializes the JavaFX toolkit, which the background scan needs to
        // update the files on the FX thread.
    }

    @Test
    @DisplayName("フォルダの走査はバックグラウンドで行われ、完了すると一覧が更新される")
    void scanning_a_folder_runs_in_the_background_and_updates_the_files_when_finished(
        FxRobot robot) throws Exception {
        Files.createFile(folder.resolve("song1.mp3"));
        Files.createFile(folder.resolve("song2.m4a"));
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()),
            new SettingsAppModel(new NullSettingsRepository())
        );

        // AtomicInteger is a mutable box to carry the size measured on the FX
        // thread out to the test thread. -1 means "not measured yet".
        var sizeRightAfterCall = new AtomicInteger(-1);
        robot.interact(() -> {
            appModel.scanFolder(folder);
            sizeRightAfterCall.set(appModel.getFiles().size());
        });

        assertEquals(0, sizeRightAfterCall.get());
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> appModel.getFiles().size() == 2);
    }

    @Test
    @DisplayName("スキャン中はscanningがtrueになり、完了するとfalseになる")
    void scanning_is_true_while_a_scan_runs_and_false_when_it_finishes(FxRobot robot)
        throws Exception {
        Files.createFile(folder.resolve("song1.mp3"));
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()),
            new SettingsAppModel(new NullSettingsRepository())
        );

        var scanningRightAfterCall = new AtomicBoolean(false);
        robot.interact(() -> {
            appModel.scanFolder(folder);
            scanningRightAfterCall.set(appModel.scanningProperty().get());
        });

        assertTrue(scanningRightAfterCall.get());
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> !appModel.scanningProperty().get());
    }

    @Test
    @DisplayName("スキャンが失敗してもscanningはfalseに戻る")
    void scanning_returns_to_false_when_the_scan_fails(FxRobot robot) throws Exception {
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()),
            new SettingsAppModel(new NullSettingsRepository())
        );

        robot.interact(() -> appModel.scanFolder(folder.resolve("missing")));

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> !appModel.scanningProperty().get());
    }

    @Test
    @DisplayName("スキャン中に読み込んだファイルのパスがscanningFileに反映される")
    void the_path_of_the_file_being_read_is_reflected_in_scanning_file(FxRobot robot)
        throws Exception {
        var file = Files.createFile(folder.resolve("song1.mp3"));
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()),
            new SettingsAppModel(new NullSettingsRepository())
        );

        robot.interact(() -> appModel.scanFolder(folder));

        // Updates are coalesced on the FX thread, so the property eventually
        // holds the path of the last read file.
        WaitForAsyncUtils.waitFor(
            5,
            TimeUnit.SECONDS,
            () -> file.toString().equals(appModel.scanningFileProperty().get())
        );
    }

    @Test
    @DisplayName("設定のライブラリフォルダが変更されると、新しいフォルダが走査され一覧が更新される")
    void changing_the_music_library_folder_in_the_settings_scans_the_new_folder(FxRobot robot)
        throws Exception {
        var folderA = Files.createDirectories(folder.resolve("a"));
        var folderB = Files.createDirectories(folder.resolve("b"));
        Files.createFile(folderA.resolve("song1.mp3"));
        Files.createFile(folderB.resolve("song2.mp3"));
        Files.createFile(folderB.resolve("song3.m4a"));
        var settingsAppModel = new SettingsAppModel(new NullSettingsRepository());
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()), settingsAppModel
        );

        robot.interact(() -> settingsAppModel.setSettings(new Settings(folderA)));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> appModel.getFiles().size() == 1);

        robot.interact(() -> settingsAppModel.setSettings(new Settings(folderB)));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> appModel.getFiles().size() == 2);
    }

    @Test
    @DisplayName("rescanを呼ぶと、最後に走査したフォルダが再走査され一覧が更新される")
    void rescan_scans_the_last_scanned_folder_again(FxRobot robot) throws Exception {
        Files.createFile(folder.resolve("song1.mp3"));
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()),
            new SettingsAppModel(new NullSettingsRepository())
        );
        robot.interact(() -> appModel.scanFolder(folder));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> appModel.getFiles().size() == 1);
        Files.createFile(folder.resolve("song2.mp3"));

        robot.interact(() -> appModel.rescan());

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> appModel.getFiles().size() == 2);
    }

    @Test
    @DisplayName("rescanを呼ぶと、削除されたファイルのエントリがライブラリDBから消える")
    void rescan_removes_the_entries_of_deleted_files_from_the_library_database(FxRobot robot)
        throws Exception {
        var kept = Files.write(folder.resolve("song1.mp3"), new byte[42]);
        var deleted = Files.write(folder.resolve("song2.mp3"), new byte[42]);
        var database = new InMemoryLibraryRepository();
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(database),
            new SettingsAppModel(new NullSettingsRepository())
        );
        robot.interact(() -> appModel.scanFolder(folder));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> appModel.getFiles().size() == 2);
        Files.delete(deleted);

        robot.interact(() -> appModel.rescan());

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> appModel.getFiles().size() == 1);
        assertEquals(Set.of(kept), database.paths());
    }

    @Test
    @DisplayName("rescanを呼んでも、変更されていないファイルのメタデータはDBのキャッシュが使われる")
    void rescan_uses_the_cached_metadata_of_an_unchanged_file(FxRobot robot) throws Exception {
        var file = Files.write(folder.resolve("song1.mp3"), new byte[42]);
        var cachedTag = new TrackMetadata(
            "Cached Title", "Cached Artist", "Cached Album", "Cached Album Artist", "3", "2020"
        );
        var database = new InMemoryLibraryRepository();
        database.save(new MusicFile(file, 42, cachedTag), Files.getLastModifiedTime(file));
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(database),
            new SettingsAppModel(new NullSettingsRepository())
        );
        robot.interact(() -> appModel.scanFolder(folder));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> appModel.getFiles().size() == 1);

        robot.interact(() -> appModel.rescan());

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> !appModel.scanningProperty().get());
        assertEquals(List.of(new MusicFile(file, 42, cachedTag)), appModel.getFiles());
    }

    private static class InMemoryLibraryRepository implements LibraryRepository {
        private record CachedEntry(long size, FileTime lastModified, TrackMetadata tag) {
        }

        private final Map<Path, CachedEntry> entries = new HashMap<>();

        @Override
        public Optional<TrackMetadata> find(Path path, long size, FileTime lastModified) {
            var entry = entries.get(path);
            if (entry == null || entry.size != size
                || entry.lastModified.compareTo(lastModified) < 0) {
                return Optional.empty();
            }
            return Optional.of(entry.tag);
        }

        @Override
        public void save(MusicFile file, FileTime lastModified) {
            entries.put(file.path(), new CachedEntry(file.size(), lastModified, file.tag()));
        }

        @Override
        public List<Path> findAllPaths() {
            return List.copyOf(entries.keySet());
        }

        @Override
        public void delete(Path path) {
            entries.remove(path);
        }

        Set<Path> paths() {
            return Set.copyOf(entries.keySet());
        }
    }

    @Test
    @DisplayName("走査結果は、サイズ付きのMusicFileとして一覧に保持される")
    void the_scanned_files_are_held_in_the_list_as_music_files_with_their_sizes(FxRobot robot)
        throws Exception {
        var file = folder.resolve("song1.mp3");
        Files.write(file, new byte[42]);
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()),
            new SettingsAppModel(new NullSettingsRepository())
        );

        robot.interact(() -> appModel.scanFolder(folder));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> !appModel.getFiles().isEmpty());

        List<MusicFile> files = appModel.getFiles();
        assertEquals(List.of(new MusicFile(file, 42)), files);
    }
}
