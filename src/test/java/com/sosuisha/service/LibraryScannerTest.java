package com.sosuisha.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
        List<Path> paths = scanner.scan(folder);

        assertEquals(List.of(first, third), paths.stream().sorted().toList());
    }

    @Test
    @DisplayName("大文字の拡張子のファイルも取得できる")
    void returns_files_with_uppercase_extension() throws Exception {
        var upper = Files.createFile(folder.resolve("fourth.MP3"));

        var scanner = new LibraryScanner();
        List<Path> paths = scanner.scan(folder);

        assertEquals(List.of(upper), paths);
    }

    @Test
    @DisplayName("mp3ファイルを取得できる")
    void returns_mp3_files() throws Exception {
        var mp3 = Files.createFile(folder.resolve("first.mp3"));

        var scanner = new LibraryScanner();
        List<Path> paths = scanner.scan(folder);

        assertEquals(List.of(mp3), paths);
    }

    @Test
    @DisplayName("m4aファイルを取得できる")
    void returns_m4a_files() throws Exception {
        var m4a = Files.createFile(folder.resolve("fifth.m4a"));

        var scanner = new LibraryScanner();
        List<Path> paths = scanner.scan(folder);

        assertEquals(List.of(m4a), paths);
    }
}
