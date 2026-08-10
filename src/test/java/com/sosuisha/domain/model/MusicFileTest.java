package com.sosuisha.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MusicFileTest {
    @Test
    @DisplayName("MusicFileはパスとサイズを持つ")
    void music_file_has_a_path_and_a_size() {
        var musicFile = new MusicFile(Path.of("a.mp3"), 100);

        assertEquals(Path.of("a.mp3"), musicFile.path());
        assertEquals(100, musicFile.size());
    }
}
