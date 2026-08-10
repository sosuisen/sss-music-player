package com.sosuisha.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.domain.model.MusicFile;

class DuplicateFileMoverTest {
    @TempDir
    Path folder;

    @Test
    @DisplayName("各グループの最初の1つ以外のファイルが、duplicatesフォルダへ移動される")
    void moves_all_but_the_first_file_of_each_group_to_the_duplicates_folder() throws Exception {
        var musicFolder = Files.createDirectories(folder.resolve("music"));
        var kept = Files.createFile(
            Files.createDirectories(musicFolder.resolve("a")).resolve("dup.mp3")
        );
        var moved = Files.createFile(
            Files.createDirectories(musicFolder.resolve("b")).resolve("dup.mp3")
        );
        var duplicatesFolder = folder.resolve("duplicates");
        var group = new DuplicatedItems(
            "dup.mp3",
            List.of(new MusicFile(kept, 0), new MusicFile(moved, 0))
        );
        var mover = new DuplicateFileMover(duplicatesFolder, folder.resolve("duplicates.log"));

        mover.moveDuplicates(List.of(group));

        assertTrue(Files.exists(kept));
        assertFalse(Files.exists(moved));
        assertTrue(Files.exists(duplicatesFolder.resolve("dup.mp3")));
    }

    @Test
    @DisplayName("duplicatesフォルダに同名ファイルがあると、移動先のファイル名は連番付きに変わる")
    void the_destination_file_name_gets_a_sequence_number_when_the_name_already_exists()
        throws Exception {
        var musicFolder = Files.createDirectories(folder.resolve("music"));
        var kept = Files.createFile(
            Files.createDirectories(musicFolder.resolve("a")).resolve("dup.mp3")
        );
        var moved = Files.createFile(
            Files.createDirectories(musicFolder.resolve("b")).resolve("dup.mp3")
        );
        var duplicatesFolder = Files.createDirectories(folder.resolve("duplicates"));
        Files.writeString(duplicatesFolder.resolve("dup.mp3"), "already there");
        var group = new DuplicatedItems(
            "dup.mp3",
            List.of(new MusicFile(kept, 0), new MusicFile(moved, 0))
        );
        var mover = new DuplicateFileMover(duplicatesFolder, folder.resolve("duplicates.log"));

        mover.moveDuplicates(List.of(group));

        assertFalse(Files.exists(moved));
        assertTrue(Files.exists(duplicatesFolder.resolve("dup_1.mp3")));
        assertEquals("already there", Files.readString(duplicatesFolder.resolve("dup.mp3")));
    }

    @Test
    @DisplayName("移動したファイルごとに、日付・移動元パス・移動先ファイル名がログに1行ずつ記録される")
    void logs_the_date_the_source_path_and_the_destination_file_name_for_each_moved_file()
        throws Exception {
        var musicFolder = Files.createDirectories(folder.resolve("music"));
        var kept = Files.createFile(
            Files.createDirectories(musicFolder.resolve("a")).resolve("dup.mp3")
        );
        var moved = Files.createFile(
            Files.createDirectories(musicFolder.resolve("b")).resolve("dup.mp3")
        );
        var duplicatesFolder = folder.resolve("duplicates");
        var logFile = folder.resolve("duplicates.log");
        var group = new DuplicatedItems(
            "dup.mp3",
            List.of(new MusicFile(kept, 0), new MusicFile(moved, 0))
        );
        var mover = new DuplicateFileMover(duplicatesFolder, logFile);

        mover.moveDuplicates(List.of(group));

        assertEquals(
            List.of(LocalDate.now() + "," + moved + ",dup.mp3"),
            Files.readAllLines(logFile)
        );
    }
}
