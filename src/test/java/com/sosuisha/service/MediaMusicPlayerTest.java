package com.sosuisha.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javafx.scene.media.Media;

// The tests create Media only. A MediaPlayer needs the JavaFX toolkit and
// plays real audio, which is too unstable for the unit tests.
class MediaMusicPlayerTest {
    @Test
    @DisplayName("濁点を結合文字で書いた（NFDの）フォルダー名にある曲のMediaを作れる")
    void creates_the_media_of_a_track_in_a_folder_whose_name_uses_a_combining_voiced_mark()
        throws IOException {
        // Not @TempDir. Media keeps the file open, and Windows then refuses
        // to delete the folder. A folder in target/ is deleted by mvn clean.
        var tempDir = Files.createTempDirectory(
            Files.createDirectories(Path.of("target/media-test")), "nfd"
        );
        // "ク" followed by the combining voiced mark. It looks like "グ", but
        // the file system keeps the two code points as they are.
        var folder = Files.createDirectory(tempDir.resolve("\u30AF\u3099"));
        var track = folder.resolve("tagged.mp3");
        Files.copy(Path.of("src/test/resources/id3/tagged.mp3"), track);

        assertDoesNotThrow(() -> new Media(MediaMusicPlayer.mediaUri(track)));
    }
}
