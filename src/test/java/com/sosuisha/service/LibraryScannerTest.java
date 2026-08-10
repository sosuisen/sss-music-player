package com.sosuisha.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sosuisha.domain.model.MusicFile;

class LibraryScannerTest {
    @TempDir
    Path folder;

    @Test
    @DisplayName("フォルダパスを指定すると、フォルダ直下とサブフォルダ内の対応形式のみのパスのリストを返す")
    void returns_paths_of_only_supported_formats_in_the_folder_and_subfolders() throws Exception {
        var first = Files.createFile(folder.resolve("first.mp3"));
        Files.createFile(folder.resolve("second.txt"));
        var sub = Files.createDirectory(folder.resolve("sub"));
        var third = Files.createFile(sub.resolve("third.mp3"));

        var scanner = new LibraryScanner();
        var musicFiles = scanner.scan(folder);

        assertEquals(
            List.of(first, third),
            musicFiles.stream().map(MusicFile::path).sorted().toList()
        );
    }

    @Test
    @DisplayName("大文字の拡張子のファイルも取得できる")
    void returns_files_with_uppercase_extension() throws Exception {
        var upper = Files.createFile(folder.resolve("fourth.MP3"));

        var scanner = new LibraryScanner();
        var musicFiles = scanner.scan(folder);

        assertEquals(List.of(upper), musicFiles.stream().map(MusicFile::path).toList());
    }

    @Test
    @DisplayName("mp3ファイルを取得できる")
    void returns_mp3_files() throws Exception {
        var mp3 = Files.createFile(folder.resolve("first.mp3"));

        var scanner = new LibraryScanner();
        var musicFiles = scanner.scan(folder);

        assertEquals(List.of(mp3), musicFiles.stream().map(MusicFile::path).toList());
    }

    @Test
    @DisplayName("m4aファイルを取得できる")
    void returns_m4a_files() throws Exception {
        var m4a = Files.createFile(folder.resolve("fifth.m4a"));

        var scanner = new LibraryScanner();
        var musicFiles = scanner.scan(folder);

        assertEquals(List.of(m4a), musicFiles.stream().map(MusicFile::path).toList());
    }

    @Test
    @DisplayName("走査結果の各ファイルは、そのファイルサイズを持つ")
    void each_scanned_file_has_its_file_size() throws Exception {
        var file = folder.resolve("first.mp3");
        Files.write(file, new byte[123]);

        var scanner = new LibraryScanner();
        List<MusicFile> musicFiles = scanner.scan(folder);

        assertEquals(List.of(new MusicFile(file, 123)), musicFiles);
    }
}
