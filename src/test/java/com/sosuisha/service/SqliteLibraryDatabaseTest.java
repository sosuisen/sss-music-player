package com.sosuisha.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.TrackMetadata;

class SqliteLibraryDatabaseTest {
    @TempDir
    Path folder;

    @AfterEach
    void cleanup() {
        System.clearProperty("sss.library.db");
    }

    @Test
    @DisplayName("システムプロパティsss.library.dbが指定されていると、その値がDBファイルのパスとして解決される")
    void resolves_the_database_file_from_the_system_property_when_it_is_set() {
        System.setProperty("sss.library.db", "custom/library.db");

        assertEquals(Path.of("custom", "library.db"), SqliteLibraryDatabase.resolveFile());
    }

    @Test
    @DisplayName("デフォルトのDBファイルは、ユーザホームの.sss-music-player/library.dbである")
    void default_file_is_library_db_in_the_sss_music_player_folder_in_the_user_home() {
        assertEquals(
            Path.of(System.getProperty("user.home"), ".sss-music-player", "library.db"),
            SqliteLibraryDatabase.DEFAULT_FILE
        );
    }

    @Test
    @DisplayName("システムプロパティsss.library.dbが未指定の場合、デフォルトのDBファイルに解決される")
    void resolves_the_database_file_to_the_default_file_when_the_system_property_is_not_set() {
        System.clearProperty("sss.library.db");

        assertEquals(SqliteLibraryDatabase.DEFAULT_FILE, SqliteLibraryDatabase.resolveFile());
    }

    @Test
    @DisplayName("DBファイルの親フォルダがなければ作成される")
    void creating_the_database_creates_the_parent_folder_if_it_does_not_exist() {
        var file = folder.resolve("parent").resolve("library.db");

        new SqliteLibraryDatabase(file);

        assertTrue(Files.exists(file));
    }

    @Test
    @DisplayName("保存したエントリを同じパス・サイズ・更新日時でfindするとメタデータを返す")
    void find_returns_saved_metadata_for_matching_path_size_and_last_modified() throws Exception {
        var database = new SqliteLibraryDatabase(folder.resolve("library.db"));
        var file = folder.resolve("song.mp3");
        var tag = new TrackMetadata(
            "Saved Song", "Saved Artist", "Saved Album", "Saved Album Artist", "2", "2010"
        );
        var lastModified = FileTime.fromMillis(1_000_000_000L);
        database.save(new MusicFile(file, 123, tag), lastModified);

        assertEquals(Optional.of(tag), database.find(file, 123, lastModified));
    }

    @Test
    @DisplayName("マッチするエントリが無い場合、findは空のOptionalを返す")
    void find_returns_empty_optional_when_no_entry_matches() throws Exception {
        var database = new SqliteLibraryDatabase(folder.resolve("library.db"));

        assertEquals(
            Optional.empty(),
            database.find(folder.resolve("unknown.mp3"), 123, FileTime.fromMillis(1_000L))
        );
    }

    @Test
    @DisplayName("同じパスを再保存すると上書きされる")
    void saving_the_same_path_again_overwrites_the_entry() throws Exception {
        var database = new SqliteLibraryDatabase(folder.resolve("library.db"));
        var file = folder.resolve("song.mp3");
        var oldTag = new TrackMetadata(
            "Old Song", "Old Artist", "Old Album", "Old Album Artist", "1", "2000"
        );
        var newTag = new TrackMetadata(
            "New Song", "New Artist", "New Album", "New Album Artist", "2", "2010"
        );
        database.save(new MusicFile(file, 123, oldTag), FileTime.fromMillis(1_000L));

        database.save(new MusicFile(file, 456, newTag), FileTime.fromMillis(2_000L));

        assertEquals(Optional.of(newTag), database.find(file, 456, FileTime.fromMillis(2_000L)));
        assertEquals(Optional.empty(), database.find(file, 123, FileTime.fromMillis(1_000L)));
    }
}
