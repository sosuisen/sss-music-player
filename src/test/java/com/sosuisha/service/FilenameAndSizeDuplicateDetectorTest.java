package com.sosuisha.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.domain.model.MusicFile;

class FilenameAndSizeDuplicateDetectorTest {
    @Test
    @DisplayName("ファイル名とサイズが両方同じファイルだけが、重複グループになる")
    void only_files_with_the_same_name_and_the_same_size_form_a_duplicated_group() {
        var sameA = new MusicFile(Path.of("a/dup.mp3"), 100);
        var sameB = new MusicFile(Path.of("b/dup.mp3"), 100);
        var differentSize = new MusicFile(Path.of("c/dup.mp3"), 200);
        var detector =
            new FilenameAndSizeDuplicateDetector(List.of(sameA, sameB, differentSize));

        var duplicatedItems = detector.detect();

        assertEquals(
            List.of(new DuplicatedItems("dup.mp3", List.of(sameA, sameB))),
            duplicatedItems
        );
    }
}
