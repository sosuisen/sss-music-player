package com.sosuisha.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;

// A MediaPlayer reports most failures after it has opened the file, on the
// JavaFX application thread. The extension starts the JavaFX toolkit for it.
@ExtendWith(ApplicationExtension.class)
class MediaMusicPlayerPlaybackTest {
    @Test
    @DisplayName("開いたあとに再生に失敗すると、失敗を通知し、何も読み込んでいない状態になる")
    void a_failure_after_opening_the_file_is_reported_and_nothing_stays_loaded(FxRobot robot)
        throws Exception {
        // Not @TempDir. Media keeps the file open, and Windows then refuses
        // to delete the folder. A folder in target/ is deleted by mvn clean.
        var folder = Files.createTempDirectory(
            Files.createDirectories(Path.of("target/media-test")), "broken"
        );
        var broken = writeZeroFilledMp3(folder);
        var player = new MediaMusicPlayer();
        var reported = new CompletableFuture<String>();
        robot.interact(() -> {
            player.setOnError(reported::complete);
            player.play(broken);
        });

        var reason = reported.get(10, TimeUnit.SECONDS);

        assertTrue(reason.contains("ERROR_MEDIA_INVALID"), reason);
        var loaded = new CompletableFuture<Optional<Path>>();
        robot.interact(() -> loaded.complete(player.playingPath()));
        assertEquals(Optional.empty(), loaded.get());
    }

    // The extension says mp3, but the content is not audio. Media accepts the
    // file, and the player fails after it has opened the file.
    private static Path writeZeroFilledMp3(Path folder) throws IOException {
        return Files.write(folder.resolve("broken.mp3"), new byte[4096]);
    }
}
